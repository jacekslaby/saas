package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Logic to build new value for an Entity based on received Request.
 * Depending on Request an Entity may be: created, deleted, updated.
 *
 * Logic is implemented as ValueJoiner because it is used in KStreams processing.
 * See also:
 * https://kafka.apache.org/21/javadoc/org/apache/kafka/streams/kstream/ValueJoiner.html
 */
public class CommandExecutor implements ValueJoiner<SpecificRecord, SpecificRecord, EntityV1> {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    // Dedicated value returned when unsupported command was received.
    public static final EntityV1 UKNOWN_ENTITY_TO_BE_IGNORED = EntityV1.newBuilder()
            .setUuid("dummy")
            .setEntryDate(System.currentTimeMillis())
            .setEntityTypeName("dummy")
            .setEntitySubdomainName("dummy")
            .setEntityIdInSubdomain("UKNOWN_ENTITY_TO_BE_IGNORED")
            .build();

    // Dedicated value returned when a create command was received for already existing entity.
    public static final EntityV1 ALREADY_EXISTING_ENTITY_TO_BE_IGNORED = EntityV1.newBuilder()
            .setUuid("dummy")
            .setEntryDate(System.currentTimeMillis())
            .setEntityTypeName("dummy")
            .setEntitySubdomainName("dummy")
            .setEntityIdInSubdomain("ALREADY_EXISTING_ENTITY_TO_BE_IGNORED")
            .build();

    // Dedicated value returned when a delete command was received for not existing entity.
    public static final EntityV1 NOT_EXISTING_ENTITY_TO_BE_IGNORED = EntityV1.newBuilder()
            .setUuid("dummy")
            .setEntryDate(System.currentTimeMillis())
            .setEntityTypeName("dummy")
            .setEntitySubdomainName("dummy")
            .setEntityIdInSubdomain("NOT_EXISTING_ENTITY_TO_BE_IGNORED")
            .build();


    @Override
    public EntityV1 apply(SpecificRecord command, SpecificRecord currentEntityValue) {

        String commandTypeName = command.getSchema().getFullName();

        if ( commandTypeName.equals(CreateEntityRequestV1.class.getName()) ) {
            // (Note: This cast is 'legal' because we use SpecificRecord and SpecificAvroSerde. )
            CreateEntityRequestV1 request = (CreateEntityRequestV1) command;
            logger.info("CommandExecutor.apply: CreateEntityRequestV1: uuid = {}", request.getUuid() );
            return createEntity(request, currentEntityValue);

        } else if ( commandTypeName.equals(DeleteEntityRequestV1.class.getName()) ) {
            DeleteEntityRequestV1 request = (DeleteEntityRequestV1) command;
            logger.info("CommandExecutor.apply: DeleteEntityRequestV1: uuid = {}", request.getUuid());
            return deleteEntity(request, currentEntityValue);

        } else {
            logger.info("CommandExecutor.apply: Uknown request '{}'. Command ignored.", commandTypeName);
            return UKNOWN_ENTITY_TO_BE_IGNORED;
        }
    }

    private EntityV1 createEntity(CreateEntityRequestV1 command, SpecificRecord currentEntityValue) {
        if (currentEntityValue != null) {

            // We do not change the already existing entity.  (btw: there is PutEntityRequest for this)
            logger.info("CommandExecutor.apply: Already existing entity. Command ignored.");
            return ALREADY_EXISTING_ENTITY_TO_BE_IGNORED;

        } else {
            // We need to create a new entity.
            logger.info("CommandExecutor.apply: Entity created.");
            return createNewFromRequest(command);
        }
    }

    private EntityV1 deleteEntity(DeleteEntityRequestV1 command, SpecificRecord currentEntityValue) {
        if (currentEntityValue != null) {

            // We delete the existing entity. (i.e. we return null as a tombstone)
            logger.info("CommandExecutor.apply: Entity deleted.");
            return null;

        } else {
            // We do not do anything for the already not existing entity.
            logger.info("CommandExecutor.apply: Not existing entity. Command ignored.");
            return NOT_EXISTING_ENTITY_TO_BE_IGNORED;
        }
    }

    private EntityV1 createNewFromRequest(CreateEntityRequestV1 createEntityRequest) {

        return EntityV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName( createEntityRequest.getEntityTypeName() )
                .setEntitySubdomainName( createEntityRequest.getEntitySubdomainName() )
                .setEntityIdInSubdomain( createEntityRequest.getEntityIdInSubdomain() )
                .setEntityAttributes( createEntityRequest.getEntityAttributes() )
                .build();
    }
}
