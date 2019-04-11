package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.commandsmodel.*;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1FieldNames;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.*;

import java.util.*;

/**
 * Logic to build new value for an Entity based on received Request.
 * Depending on Request an Entity may be: created, deleted, updated.
 *
 * Logic is implemented as Processor<K,V> because it is used in Topology-based processing.
 * (i.e. Processor API instead of StreamsBuilder API)
 *
 * The in-memory state store is provided by the Topology.
 * Its name is provided to CommandProcessor constructor
 *  and is used to lookup store instance from ProcessorContext received in {@link #init(ProcessorContext)}.
 *
 * See also:
 * https://kafka.apache.org/21/javadoc/org/apache/kafka/streams/Topology.html#
     addProcessor-java.lang.String-org.apache.kafka.streams.processor.ProcessorSupplier-java.lang.String...-
 */
public class CommandProcessor implements Processor<String, GenericRecord> {

    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);
    // We use a dedicated requestLogger in order to be able to separate log entries
    //  made during a request processing (which is very frequent and produces lots of logging)
    //  from the other ones. (btw: The other ones convey status and we want to have them,
    //   i.e. we want to avoid a logfile being rolled over.)
    private static final Logger requestLogger = LoggerFactory.getLogger(
            CommandProcessor.class.getName() + ".requests");
    private static final String MDC_TASK_ID = "task-id";
    private static final String MDC_KEY_UUID = "uuid";
    private static final String MDC_KEY_COMMAND_TYPE = "command-type";
    private static final String CREATE_COMMAND = CreateEntityRequestV1.class.getSimpleName();
    private static final String DELETE_COMMAND = DeleteEntityRequestV1.class.getSimpleName();
    private static final String RESYNC_START_COMMAND = ResyncAllStartSubdomainRequestV1.class.getSimpleName();
    private static final String RESYNC_END_COMMAND = ResyncAllEndSubdomainRequestV1.class.getSimpleName();

    private ProcessorContext context;
    private String entitiesStoreName;
    private KeyValueStore<String, GenericRecord> entityKVStateStore;

    // The current Entity schema may be enhanced with additional fields during runtime.
    // (It happens when CommandProcessor receives a request with additional entity attributes.
    //  In such case a new schema is created in process() method.
    //  It is also registered in Schema Registry by Avro SerDe.)
    // (Note: This approach is thread safe due to KStreams threading model:
    //   "Each thread can execute one or more stream tasks".
    //   See also: https://docs.confluent.io/current/streams/architecture.html#threading-model )
    private Schema currentEntitySchema;
    private Schema currentAttributesSchema;
    private Set<String> currentAttributesSchemaFieldNamesSet;
    private Map<String, Set<String>> subdomainsInResync =
            new HashMap<>(); // key is subdomain name (because subdomains are resynchronized independently)

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

    @SuppressWarnings("unchecked")
    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        entityKVStateStore = (KeyValueStore<String, GenericRecord>) context.getStateStore(entitiesStoreName);

        logger.info("init: applicationId={}, taskId={}, processor={}, entityKVStateStore={}," +
                        " entityKVStateStore.approximateNumEntries() = {}, appConfigs={}",
                context.applicationId(), context.taskId(), this,
                entityKVStateStore, entityKVStateStore.approximateNumEntries(), context.appConfigs());
    }

    @Override
    public void process(String messageKey, GenericRecord command) {

        boolean runtimeExceptionWasThrown = true;

        try {
            String commandTypeName = command.getSchema().getFullName();

            // (Ugly if-else statement because we depend on getSchema() API.)

            if (commandTypeName.equals(CreateEntityRequestV1.class.getName())) {
                createEntity(command);

            } else if (commandTypeName.equals(DeleteEntityRequestV1.class.getName())) {
                deleteEntity(command);

            } else if (commandTypeName.equals(ResyncAllStartSubdomainRequestV1.class.getName())) {
                startSubdomainResync(command);

            } else if (commandTypeName.equals(ResyncAllEndSubdomainRequestV1.class.getName())) {
                endSubdomainResync(command);

            } else {
                requestLogger.info("RESULT: Command ignored. Uknown command type '{}' in request {}. ",
                        commandTypeName, command);
            }
            runtimeExceptionWasThrown = false;

        } finally {
            if (runtimeExceptionWasThrown) {
                // Let's log additional context details in order to facilitate debugging.
                logger.error("FAILURE: RuntimeException was thrown. Processing context: " +
                                "applicationId={}, taskId={}, messageKey '{}', command '{}'",
                        context.applicationId(), context.taskId(), messageKey, command);
            }
            // Let's remove the diagnostic context.
            MDC.remove(MDC_KEY_UUID);
            MDC.remove(MDC_KEY_COMMAND_TYPE);
        }
    }

    @Override
    public void close() {
        logger.info("close: applicationId={}, taskId={}, processor={}, " +
                        "entityKVStateStore={}, entityKVStateStore.approximateNumEntries() = {}, appConfigs={}",
                context.applicationId(), context.taskId(), this,
                entityKVStateStore, entityKVStateStore.approximateNumEntries(), context.appConfigs());
    }

    private void createEntity(GenericRecord createEntityRequest) {

        fillMDCAndLogStart(CREATE_COMMAND, createEntityRequest, CreateEntityRequestV1FieldNames.UUID);

        String entityIdInSubdomain = createEntityRequest.get(
                CreateEntityRequestV1FieldNames.ENTITY_ID_IN_SUBDOMAIN).toString();
        String entitySubdomainName = createEntityRequest.get(
                CreateEntityRequestV1FieldNames.ENTITY_SUBDOMAIN_NAME).toString();

        // @FUTURE On entities topic the message key needs to be built also from KR_REPOSITORY_NAME
        //  (needed in case when different environments (e.g. prod, ref, test) (or clientA, clientB, multitenancy)
        //   use the same topics)
        //
        // For entities topic the message key contains:
        //   {entity_subdomain_name, entity_id_in_subdomain} (because it is a compacted topic)
        // For state store the entityKey contains:
        //   {entity_subdomain_name, entity_id_in_subdomain} (because it is a store of all subdomains)
        // (E.g. different adapters may provide the same notification_identifier and k-repository should store it.
        //   If need be it may be reconciled by other subsystem downstream.)
        // (Note: It would be safer to use a structure more complicated
        //  than just a String, (e.g. a two field structure or an array)
        //  but it would not be user friendly for browsing topic contents.
        //  So, we assume that character ';' is not used in subdomain names.)
        // (Note: It is more efficient to have ID first (rather than Subdomain name),
        //   because it speeds up equals comparisons.)
        String entityKey = entityIdInSubdomain + ";" + entitySubdomainName;
        if (requestLogger.isDebugEnabled()) {
            requestLogger.debug("entityKey = {}, entityIdInSubdomain = {}, entitySubdomainName = {}",
                    entityKey, entityIdInSubdomain, entitySubdomainName);
            requestLogger.debug("entityKVStateStore = {}, entityKVStateStore.approximateNumEntries() = {}",
                    entityKVStateStore, entityKVStateStore.approximateNumEntries());
        }
        GenericRecord currentEntityValue = entityKVStateStore.get(entityKey);

        Set<String> keysOfSurvivingEntities = subdomainsInResync.get(entitySubdomainName);
        if (keysOfSurvivingEntities != null) {
            // We need to gather this entity for survival.
            keysOfSurvivingEntities.add(entityIdInSubdomain);
        }

        if (currentEntityValue != null) {
            // We do not change the already existing entity.  (btw: there is PutEntityRequest for this)
            requestLogger.info("RESULT: Command ignored. Already existing entity with key '{}'.", entityKey);

        } else {
            // We need to create a new entity.
            GenericRecord newEntityValue = createNewEntity(createEntityRequest);

            // Forward it in order to be published on entities topic
            //  and save it in the local store.
            context.forward(entityKey, newEntityValue);
            entityKVStateStore.put(entityKey, newEntityValue);

            requestLogger.info("RESULT: Entity created. New entity with key '{}'.", entityKey);
        }
    }

    private void fillMDCAndLogStart(String commandType, GenericRecord request, String uuidFieldName) {
        // Let's stamp each logged entry with request UUID.
        MDC.put(MDC_KEY_UUID, Objects.toString(request.get(uuidFieldName)));
        MDC.put(MDC_KEY_COMMAND_TYPE, commandType);
        MDC.put(MDC_TASK_ID, Objects.toString(context.taskId()));

        requestLogger.debug("START: request = {}", request);
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
        newEntity.put(EntityV1FieldNames.ENTITY_TYPE_NAME,
                createEntityRequest.get(CreateEntityRequestV1FieldNames.ENTITY_TYPE_NAME));
        newEntity.put(EntityV1FieldNames.ENTITY_SUBDOMAIN_NAME,
                createEntityRequest.get(CreateEntityRequestV1FieldNames.ENTITY_SUBDOMAIN_NAME));
        newEntity.put(EntityV1FieldNames.ENTITY_ID_IN_SUBDOMAIN,
                createEntityRequest.get(CreateEntityRequestV1FieldNames.ENTITY_ID_IN_SUBDOMAIN));
        GenericRecord newAttributes = createAttributes( entityAttributesFromRequest );
        if (newAttributes != null) {
            newEntity.put(EntityV1FieldNames.ATTRIBUTES, newAttributes);
        }

        return newEntity;
    }

    private void provideNewSchemaIfNeeded(GenericRecord entityAttributesFromRequest) {

        requestLogger.debug("provideNewSchemaIfNeeded: entityAttributesFromRequest '{}'.",
                entityAttributesFromRequest);

        if (entityAttributesFromRequest == null) {
            return; // record with attributes is optional (i.e. it may be null in Request (and in Entity))
        }

        ArrayList<Schema.Field> missingFieldsList = new ArrayList<>();

        // Let's verify whether we need to create a new Schema version for EntityV1.
        for (Schema.Field fieldFromRequest : entityAttributesFromRequest.getSchema().getFields()) {
            String fieldName = fieldFromRequest.name();
            requestLogger.debug("provideNewSchemaIfNeeded: check for existence of entity attribute '{}'.", fieldName);
            if (!currentAttributesSchemaFieldNamesSet.contains(fieldName)) {
                requestLogger.debug("provideNewSchemaIfNeeded: This attribute does not exist '{}'." +
                        " A new schema is required.", fieldName);
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

        // https://stackoverflow.com/questions/47161408/extend-avro-schema-via-java-api-by-adding-one-field

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
        // @FUTURE check if it is a union (i.e. an optional field) - if not then correct it.
        //   We want (?) all fields to be optional.
        for (Schema.Field f : missingFieldsList) {
            Schema.Field newField = new Schema.Field(f.name(), f.schema(), f.doc(), f.defaultVal());
            newFieldsList.add(newField);
        }

        // Let's create the record schema.
        //  (it is the second one in the union defined in field entity_attributes, i.e. union of {null, record})
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

        fillMDCAndLogStart(DELETE_COMMAND, deleteEntityRequest, CreateEntityRequestV1FieldNames.UUID);

        // @FUTURE On entities topic the message key needs to be built from:
        //   entity_subdomain_name + entity_id_in_subdomain.
        //  (needed in case when different environments (e.g. prod, ref, test) (or clientA, clientB, multitenancy)
        //   use the same topics)
        String entityIdInSubdomain = deleteEntityRequest.get(
                CreateEntityRequestV1FieldNames.ENTITY_ID_IN_SUBDOMAIN).toString();
        String entitySubdomainName = deleteEntityRequest.get(
                CreateEntityRequestV1FieldNames.ENTITY_SUBDOMAIN_NAME).toString();
        String entityKey = entityIdInSubdomain + ";" + entitySubdomainName;
        if (requestLogger.isDebugEnabled()) {
            requestLogger.debug("entityKey = {}, entityIdInSubdomain = {}, entitySubdomainName = {}",
                    entityKey, entityIdInSubdomain, entitySubdomainName);
            requestLogger.debug("entityKVStateStore = {}, entityKVStateStore.approximateNumEntries() = {}",
                    entityKVStateStore, entityKVStateStore.approximateNumEntries());
        }
        GenericRecord currentEntityValue = entityKVStateStore.get(entityKey);

        if (currentEntityValue != null) {
            Set<String> keysOfSurvivingEntities = subdomainsInResync.get(entitySubdomainName);
            if (keysOfSurvivingEntities != null) {
                // We need to remove it. (e.g. in case it was delivered within this resync)
                keysOfSurvivingEntities.remove(entityIdInSubdomain);
            }

            // We delete the existing entity.
            context.forward(entityKey, null);  // (i.e. we publish null as a tombstone)
            entityKVStateStore.delete(entityKey);

            requestLogger.info("RESULT: Entity deleted. Entity with key '{}'.", entityKey);

        } else {
            // We do not do anything for the already not existing entity.
            requestLogger.info("RESULT: Command ignored. Not existing entity with key '{}'.", entityKey);
        }
    }

    private void startSubdomainResync(GenericRecord resyncAllStartSubdomainRequest) {

        fillMDCAndLogStart(RESYNC_START_COMMAND, resyncAllStartSubdomainRequest,
                ResyncAllStartSubdomainRequestV1FieldNames.UUID);

        String subdomainName = resyncAllStartSubdomainRequest.get(
                ResyncAllStartSubdomainRequestV1FieldNames.ENTITY_SUBDOMAIN_NAME).toString();
        if (requestLogger.isDebugEnabled()) {
            requestLogger.debug("subdomainName = {}", subdomainName);
            requestLogger.debug("entityKVStateStore = {}, entityKVStateStore.approximateNumEntries() = {}",
                    entityKVStateStore, entityKVStateStore.approximateNumEntries());
        }

        // Let's prepare in-memory structures to gather surviving entities.
        //  (Note: we overwrite in case the same subdomain is already in resync.)
        subdomainsInResync.put(subdomainName, new HashSet<>());

        requestLogger.info("RESULT: Resync started. For domain '{}'.", subdomainName);
    }

    private void endSubdomainResync(GenericRecord resyncAllEndSubdomainRequest) {

        fillMDCAndLogStart(RESYNC_END_COMMAND, resyncAllEndSubdomainRequest,
                ResyncAllEndSubdomainRequestV1FieldNames.UUID);

        String subdomainName = resyncAllEndSubdomainRequest.get(
                ResyncAllEndSubdomainRequestV1FieldNames.ENTITY_SUBDOMAIN_NAME).toString();
        if (requestLogger.isDebugEnabled()) {
            requestLogger.debug("subdomainName = {}", subdomainName);
            requestLogger.debug("entityKVStateStore = {}, entityKVStateStore.approximateNumEntries() = {}",
                    entityKVStateStore, entityKVStateStore.approximateNumEntries());
        }

        Set<String> keysOfSurvivingEntities = subdomainsInResync.remove(subdomainName);
        if (keysOfSurvivingEntities == null) {
            requestLogger.info("RESULT: Resync end ignored. " +
                    "Earlier there was no ResyncAllStartSubdomainRequestV1 for domain '{}'.", subdomainName);
            return;
        }

        // Let's delete all not surviving entities.
        int countOfDeletedEntities = 0;
        final KeyValueIterator<String, GenericRecord> allKVIterator = entityKVStateStore.all();
        while (allKVIterator.hasNext()) {
            KeyValue<String, GenericRecord> storeEntry = allKVIterator.next();
            String entitySubdomainName = storeEntry.value.get(
                    CreateEntityRequestV1FieldNames.ENTITY_SUBDOMAIN_NAME).toString();
            String entityIdInSubdomain = storeEntry.value.get(
                    CreateEntityRequestV1FieldNames.ENTITY_ID_IN_SUBDOMAIN).toString();
            if (subdomainName.equals(entitySubdomainName)) {
                if (!keysOfSurvivingEntities.remove(entityIdInSubdomain)) {
                    // It means that this id was not delivered during resync. So we should delete it.
                    context.forward(storeEntry.key, null);  // (i.e. we publish null as a tombstone)
                    entityKVStateStore.delete(storeEntry.key);
                    countOfDeletedEntities++;
                }
            }
        }

        requestLogger.info("RESULT: Resync finished. For domain '{}', deleted entities count: {}.",
                subdomainName, String.valueOf(countOfDeletedEntities));
    }
}
