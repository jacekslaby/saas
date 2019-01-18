package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1FieldNames;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1FieldNames;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
public class CommandProcessor implements Processor<String, GenericRecord> {

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
    private Set<String> currentAttributesSchemaFieldNamesSet;

    public CommandProcessor(String entitiesStoreName) {

        this.entitiesStoreName = entitiesStoreName;
        initEntitySchema(EntityV1.getClassSchema());
    }

    private void initEntitySchema(Schema newEntitySchema) {
        currentEntitySchema = newEntitySchema;
        currentAttributesSchema = currentEntitySchema.getField(EntityV1FieldNames.ATTRIBUTES)
                .schema().getTypes().get(1); // In union 'null' is under 0 and 'record' is under 1

        // Let's build set with field names. It speeds up detection of new attributes in process().
        currentAttributesSchemaFieldNamesSet = new HashSet<>();
        for (Schema.Field fieldFromCurrent: currentAttributesSchema.getFields()) {
            currentAttributesSchemaFieldNamesSet.add(fieldFromCurrent.name());
        }
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        entityKVStateStore = (KeyValueStore<String, GenericRecord>) context.getStateStore(entitiesStoreName);
    }

    @Override
    public void process(String messageKey, GenericRecord command) {

        String commandTypeName = command.getSchema().getFullName();

        if ( commandTypeName.equals(CreateEntityRequestV1.class.getName()) ) {
            createEntity(command);

        } else if ( commandTypeName.equals(DeleteEntityRequestV1.class.getName()) ) {
            deleteEntity(command);

        } else {
            logger.info("process: Uknown request '{}'. Command ignored.", commandTypeName);
        }
    }

    @Override
    public void close() {

    }

    private void createEntity(GenericRecord createEntityRequest) {

        logger.info("process: CreateEntityRequestV1: uuid = {}", createEntityRequest.get(CreateEntityRequestV1FieldNames.UUID) );

        // @FUTURE On entities topic the message key needs to be built from entity_subdomain_name + entity_id_in_subdomain.
        //  (needed in case when different environments (e.g. prod, ref, test) (or clientA, clientB, multitenancy)
        //   use the same topics)
        String entityKey = createEntityRequest.get(CreateEntityRequestV1FieldNames.ENTITY_ID_IN_SUBDOMAIN).toString();
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

    private GenericRecord createNewEntity(GenericRecord createEntityRequest) {

        GenericRecord entityAttributesFromRequest = (GenericRecord) createEntityRequest.get(
                CreateEntityRequestV1FieldNames.ENTITY_ATTRIBUTES);

        // If unknown attributes exist in entity provided in the request
        //   then we create a new schema for EntityV1.
        provideNewSchemaIfNeeded(entityAttributesFromRequest);

        // Create a new entity and fill it with content.
        GenericRecord newEntity = new GenericData.Record(currentEntitySchema);
        newEntity.put(EntityV1FieldNames.UUID, UUID.randomUUID().toString());
        newEntity.put(EntityV1FieldNames.ENTRY_DATE, System.currentTimeMillis());
        newEntity.put(EntityV1FieldNames.ENTITY_TYPE_NAME, createEntityRequest.get(CreateEntityRequestV1FieldNames.ENTITY_TYPE_NAME));
        newEntity.put(EntityV1FieldNames.ENTITY_SUBDOMAIN_NAME, createEntityRequest.get(CreateEntityRequestV1FieldNames.ENTITY_SUBDOMAIN_NAME));
        newEntity.put(EntityV1FieldNames.ENTITY_ID_IN_SUBDOMAIN, createEntityRequest.get(CreateEntityRequestV1FieldNames.ENTITY_ID_IN_SUBDOMAIN));
        GenericRecord newAttributes = createAttributes( entityAttributesFromRequest );
        if (newAttributes != null) {
            newEntity.put(EntityV1FieldNames.ATTRIBUTES, newAttributes);
        }

        return newEntity;
    }

    private void provideNewSchemaIfNeeded(GenericRecord entityAttributesFromRequest) {

        logger.debug("provideNewSchemaIfNeeded: entityAttributesFromRequest '{}'.", entityAttributesFromRequest);

        if (entityAttributesFromRequest == null) {
            return; // record with attributes is optional (i.e. it may be null in Request (and in Entity))
        }

        ArrayList<Schema.Field> missingFieldsList = new ArrayList<>();

        // Let's verify whether we need to create a new Schema version for EntityV1.
        for (Schema.Field fieldFromRequest : entityAttributesFromRequest.getSchema().getFields()) {
            String fieldName = fieldFromRequest.name();
            logger.debug("provideNewSchemaIfNeeded: check for existence of entity attribute '{}'.", fieldName);
            if (!currentAttributesSchemaFieldNamesSet.contains(fieldName)) {
                logger.debug("provideNewSchemaIfNeeded: This attribute does not exist '{}'. A new schema is required.", fieldName);
                missingFieldsList.add(fieldFromRequest);
            }
        }

        if (missingFieldsList.size() > 0) {
            // We have at least one new field, so a new schema is required.

            // (Note: This case is not frequent (i.e. a few times a month),
            //   so code clarity is more important than performance.)

            // First we need to prepare new schema for record type. (the record with "name:Attributes" in EntityV1)
            // (based on the old schema and with new fields added)
            Schema newEntityAttributesRecordSchema = createNewSchemaForRecordEntityAttributes(missingFieldsList);

            // Then we create a new version of EntityV1 schema.
            Schema newSchema = createNewSchemaForEntity(newEntityAttributesRecordSchema);

            // Let's use the new schema for new Entity objects. (SerDe will register this schema in Schema Registry.)
            initEntitySchema(newSchema);
        }
    }

    private Schema createNewSchemaForEntity(Schema newEntityAttributesRecordSchema) {

        // The new version of the schema has the same name and namespace.
        Schema newSchema = Schema.createRecord(currentEntitySchema.getName(), currentEntitySchema.getDoc(),
                currentEntitySchema.getNamespace(), false);

        // To the new version let's add all fields existing in the current version of EntityV1 schema.
        ArrayList<Schema.Field> newFieldsList = new ArrayList<>();
        for (Schema.Field f : currentEntitySchema.getFields()) {
            Schema newFieldSchema;
            if (EntityV1FieldNames.ATTRIBUTES.equals(f.name())) {
                // For field 'attributes' we must use the new schema. (i.e. a schema with additional fields in record)
                newFieldSchema = Schema.createUnion(Schema.create(Schema.Type.NULL), newEntityAttributesRecordSchema);
            } else {
                newFieldSchema = f.schema();
            }
            Schema.Field newField = new Schema.Field(f.name(), newFieldSchema, f.doc(), f.defaultVal());
            newFieldsList.add(newField);
        }
        newSchema.setFields(newFieldsList);
        return newSchema;
    }

    private Schema createNewSchemaForRecordEntityAttributes(ArrayList<Schema.Field> missingFieldsList) {
        Schema oldEntityAttributesRecordSchema = currentEntitySchema.getField(EntityV1FieldNames.ATTRIBUTES)
                .schema().getTypes().get(1);

        // Gather existing fields.
        ArrayList<Schema.Field> newFieldsList = new ArrayList<>();
        for (Schema.Field f : oldEntityAttributesRecordSchema.getFields()) {
            Schema.Field newField = new Schema.Field(f.name(), f.schema(), f.doc(), f.defaultVal());
            newFieldsList.add(newField);
        }
        // Add new fields.
        // @FUTURE check if it is a union (i.e. an optional field) - if not then correct it. We want (?) all fields to be optional.
        for (Schema.Field f : missingFieldsList) {
            Schema.Field newField = new Schema.Field(f.name(), f.schema(), f.doc(), f.defaultVal());
            newFieldsList.add(newField);
        }

        // Let's create the record schema. (it is the second one in the union defined in field entity_attributes, i.e. union of {null, record})
        Schema newEntityAttributesRecordSchema = Schema.createRecord(oldEntityAttributesRecordSchema.getName(),
                oldEntityAttributesRecordSchema.getDoc(), oldEntityAttributesRecordSchema.getNamespace(), false);
        newEntityAttributesRecordSchema.setFields(newFieldsList);

        return newEntityAttributesRecordSchema;
    }

    private GenericRecord createAttributes(GenericRecord entityAttributesFromRequest) {

            // Let's copy all attributes provided in this Request.
        GenericRecord newAttributes = new GenericData.Record(currentAttributesSchema);
        for (Schema.Field fieldFromRequest: entityAttributesFromRequest.getSchema().getFields()) {
            newAttributes.put(fieldFromRequest.name(), entityAttributesFromRequest.get(fieldFromRequest.pos()));
        }

        return newAttributes;
    }

    private void deleteEntity(GenericRecord deleteEntityRequest) {

        logger.info("process: DeleteEntityRequestV1: uuid = {}", deleteEntityRequest.get(CreateEntityRequestV1FieldNames.UUID));

        // @FUTURE On entities topic the message key needs to be built from entity_subdomain_name + entity_id_in_subdomain.
        //  (needed in case when different environments (e.g. prod, ref, test) (or clientA, clientB, multitenancy)
        //   use the same topics)
        String entityKey = deleteEntityRequest.get(CreateEntityRequestV1FieldNames.ENTITY_ID_IN_SUBDOMAIN).toString();
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
