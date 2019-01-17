package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.EntityAttributes;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1FieldNames;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Logic to build new value for an Entity based on received Request.
 * Depending on Request an Entity may be: created, deleted, updated.
 *
 * Logic is implemented as Processor<K,V> because it is used in Topology-based processing. (i.e. Processor API instead of StreamsBuilder API)
 *
 * The in-memory state store is provided by the Topology.
 * Its name is provided to CommandProcessor constructor
 *  and is used to lookup store instance from ProcessorContext received in {@link #init(ProcessorContext)}.
 *
 * See also:
 * https://kafka.apache.org/21/javadoc/org/apache/kafka/streams/Topology.html#addProcessor-java.lang.String-org.apache.kafka.streams.processor.ProcessorSupplier-java.lang.String...-
 */
public class CommandProcessor implements Processor<String, SpecificRecord> {

    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    private ProcessorContext context;
    private String entitiesStoreName;
    private KeyValueStore<String, GenericRecord> entityKVStateStore;

    // The current Entity schema may be enhanced with additional fields during runtime.
    // (It happens when CommandProcessor receives a request with additional entity attributes.
    //  In such case a new schema is created in process() method. It is also registered in Schema Registry by Avro SerDe.)
    // (@TODO: Investigate whether KStreams assure that only one thread executes process() method. Otherwise synchronized needed.)
    private Schema currentEntitySchema;
    private Schema currentAttributesSchema;
    private Set currentAttributesSchemaFieldNamesSet;

    public CommandProcessor(String entitiesStoreName) {

        this.entitiesStoreName = entitiesStoreName;
        initEntitySchema(EntityV1.getClassSchema());
    }

    private void initEntitySchema(Schema newEntitySchema) {
        currentEntitySchema = newEntitySchema;
        currentAttributesSchema = currentEntitySchema.getField(EntityV1FieldNames.ATTRIBUTES)
                .schema().getTypes().get(1); // In union 'null' is under 0 and 'record' is under 1

        // Let's build set with field names. It speeds up detection of new attributes in process().
        currentAttributesSchemaFieldNamesSet = new HashSet();
        for (Schema.Field fieldFromCurrent: currentAttributesSchema.getFields()) {
            currentAttributesSchemaFieldNamesSet.add(fieldFromCurrent.name());
        }
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        entityKVStateStore = (KeyValueStore) context.getStateStore(entitiesStoreName);
    }

    @Override
    public void process(String messageKey, SpecificRecord command) {

        String commandTypeName = command.getSchema().getFullName();

        if ( commandTypeName.equals(CreateEntityRequestV1.class.getName()) ) {
            // (Note: The following cast operation is 'legal' because we use SpecificRecord and SpecificAvroSerde. )
            CreateEntityRequestV1 request = (CreateEntityRequestV1) command;
            createEntity(request);

        } else if ( commandTypeName.equals(DeleteEntityRequestV1.class.getName()) ) {
            DeleteEntityRequestV1 request = (DeleteEntityRequestV1) command;
            deleteEntity(request);

        } else {
            logger.info("process: Uknown request '{}'. Command ignored.", commandTypeName);
        }
    }

    @Override
    public void close() {

    }

    private void createEntity(CreateEntityRequestV1 createEntityRequest) {

        logger.info("process: CreateEntityRequestV1: uuid = {}", createEntityRequest.getUuid() );

        // @FUTURE On entities topic the message key needs to be built from entity_subdomain_name + entity_id_in_subdomain.
        //  (needed in case when different environments (e.g. prod, ref, test) (or clientA, clientB, multitenancy)
        //   use the same topics)
        String entityKey = createEntityRequest.getEntityIdInSubdomain().toString();
        if (logger.isDebugEnabled()) {
            logger.debug("process: CreateEntityRequestV1: entityKey = {}", entityKey);
            logger.debug("process: CreateEntityRequestV1: entityKVStateStore = {}, entityKVStateStore.approximateNumEntries() = {}",
                    entityKVStateStore, entityKVStateStore.approximateNumEntries());
        }
        GenericRecord currentEntityValue = entityKVStateStore.get(entityKey);

        if (currentEntityValue != null) {
            // We do not change the already existing entity.  (btw: there is PutEntityRequest for this)
            logger.info("process: CreateEntityRequestV1: Command ignored. Already existing entity with key '{}'.", entityKey);

        } else {
            // We need to create a new entity.
            GenericRecord newEntityValue = createNewEntity(createEntityRequest);

            // Forward it in order to be published on entities topic
            //  and save it in the local store.
            context.forward(entityKey, newEntityValue);
            entityKVStateStore.put(entityKey, newEntityValue);

            logger.info("process: CreateEntityRequestV1: Entity created. New entity with key '{}'.", entityKey);
        }
    }

    private GenericRecord createNewEntity(CreateEntityRequestV1 createEntityRequest) {

        GenericRecord newEntity = new GenericData.Record(currentEntitySchema);

        newEntity.put(EntityV1FieldNames.UUID, UUID.randomUUID().toString());
        newEntity.put(EntityV1FieldNames.ENTRY_DATE, System.currentTimeMillis());
        newEntity.put(EntityV1FieldNames.ENTITY_TYPE_NAME, createEntityRequest.getEntityTypeName());
        newEntity.put(EntityV1FieldNames.ENTITY_SUBDOMAIN_NAME, createEntityRequest.getEntitySubdomainName());
        newEntity.put(EntityV1FieldNames.ENTITY_ID_IN_SUBDOMAIN, createEntityRequest.getEntityIdInSubdomain());

        GenericRecord newAttributes = createAttributes( createEntityRequest.getEntityAttributes() );
        if (newAttributes != null) {
            newEntity.put(EntityV1FieldNames.ATTRIBUTES, newAttributes);
        }

        return newEntity;
    }

    private GenericRecord createAttributes(EntityAttributes entityAttributesFromRequest) {

        if (entityAttributesFromRequest == null) {
            return null; // record with attributes is optional, so it may be null in Request (and in Entity)
        }

        // Let's verify whether we need to create a new Schema for Entities.
        boolean newSchemaIsRequired = false;
        for (Schema.Field fieldFromRequest: entityAttributesFromRequest.getSchema().getFields()) {
            if (! currentAttributesSchemaFieldNamesSet.contains(fieldFromRequest.name()) ) {
                // We have at least one new field.
                newSchemaIsRequired = true;
                break;
            }
        }

        if (newSchemaIsRequired) {
            // (Note: This scenario is not frequent, a few cases a month,
            //   so code clarity is more important then performance.)

            // @TODO initEntitySchema();

            throw new RuntimeException("@TODO");
        }

        // Let's copy all attributes provided in this Request.
        GenericRecord newAttributes = new GenericData.Record(currentAttributesSchema);
        for (Schema.Field fieldFromRequest: entityAttributesFromRequest.getSchema().getFields()) {
            newAttributes.put(fieldFromRequest.name(), entityAttributesFromRequest.get(fieldFromRequest.pos()));
        }

        return newAttributes;
    }

    private void deleteEntity(DeleteEntityRequestV1 deleteEntityRequest) {

        logger.info("process: DeleteEntityRequestV1: uuid = {}", deleteEntityRequest.getUuid());

        // @FUTURE On entities topic the message key needs to be built from entity_subdomain_name + entity_id_in_subdomain.
        //  (needed in case when different environments (e.g. prod, ref, test) (or clientA, clientB, multitenancy)
        //   use the same topics)
        String entityKey = deleteEntityRequest.getEntityIdInSubdomain().toString();
        if (logger.isDebugEnabled()) {
            logger.debug("process: DeleteEntityRequestV1: entityKey = {}", entityKey);
            logger.debug("process: DeleteEntityRequestV1: entityKVStateStore = {}, entityKVStateStore.approximateNumEntries() = {}",
                    entityKVStateStore, entityKVStateStore.approximateNumEntries());
        }
        GenericRecord currentEntityValue = entityKVStateStore.get(entityKey);

        if (currentEntityValue != null) {
            // We delete the existing entity.
            context.forward(entityKey, null);  // (i.e. we publish null as a tombstone)
            entityKVStateStore.delete(entityKey);

            logger.info("process: DeleteEntityRequestV1: Entity deleted. Entity with key '{}'.", entityKey);

        } else {
            // We do not do anything for the already not existing entity.
            logger.info("process: DeleteEntityRequestV1: Command ignored. Not existing entity with key '{}'.", entityKey);
        }
    }

}
