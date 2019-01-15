package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private KeyValueStore<String, EntityV1> entityKVStateStore;

    public CommandProcessor(String entitiesStoreName) {
        this.entitiesStoreName = entitiesStoreName;
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
            logger.info("CommandProcessor.process: Uknown request '{}'. Command ignored.", commandTypeName);
        }
    }

    @Override
    public void close() {

    }

    private void createEntity(CreateEntityRequestV1 createEntityRequest) {

        logger.info("CommandProcessor.process: CreateEntityRequestV1: uuid = {}", createEntityRequest.getUuid() );

        // @FUTURE On entities topic the message key needs to be built from entity_subdomain_name + entity_id_in_subdomain.
        //  (needed in case when different environments (e.g. prod, ref, test) (or clientA, clientB, multitenancy)
        //   use the same topics)
        String entityKey = createEntityRequest.getEntityIdInSubdomain().toString();
        EntityV1 currentEntityValue = entityKVStateStore.get(entityKey);

        if (currentEntityValue != null) {
            // We do not change the already existing entity.  (btw: there is PutEntityRequest for this)
            logger.info("CommandProcessor.process: Command ignored. Already existing entity with key '{}'.", entityKey);

        } else {
            // We need to create a new entity.
            EntityV1 newEntityValue = EntityV1.newBuilder()
                    .setUuid(UUID.randomUUID().toString())
                    .setEntryDate(System.currentTimeMillis())
                    .setEntityTypeName( createEntityRequest.getEntityTypeName() )
                    .setEntitySubdomainName( createEntityRequest.getEntitySubdomainName() )
                    .setEntityIdInSubdomain( createEntityRequest.getEntityIdInSubdomain() )
                    .setEntityAttributes( createEntityRequest.getEntityAttributes() )
                    .build();

            context.forward(entityKey, newEntityValue);
            entityKVStateStore.put(entityKey, newEntityValue);

            logger.info("CommandProcessor.process: Entity created. New entity with key '{}'.", entityKey);
        }
    }

    private void deleteEntity(DeleteEntityRequestV1 deleteEntityRequest) {

        logger.info("CommandProcessor.process: DeleteEntityRequestV1: uuid = {}", deleteEntityRequest.getUuid());

        // @FUTURE On entities topic the message key needs to be built from entity_subdomain_name + entity_id_in_subdomain.
        //  (needed in case when different environments (e.g. prod, ref, test) (or clientA, clientB, multitenancy)
        //   use the same topics)
        String entityKey = deleteEntityRequest.getEntityIdInSubdomain().toString();
        EntityV1 currentEntityValue = entityKVStateStore.get(entityKey);

        if (currentEntityValue != null) {
            // We delete the existing entity.
            context.forward(entityKey, null);  // (i.e. we publish null as a tombstone)
            entityKVStateStore.delete(entityKey);

            logger.info("CommandProcessor.process: Entity deleted. Entity with key '{}'.", entityKey);

        } else {
            // We do not do anything for the already not existing entity.
            logger.info("CommandProcessor.process: Command ignored. Not existing entity with key '{}'.", entityKey);
        }
    }

}
