package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import org.apache.avro.generic.GenericRecord;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CommandExecutorTest {

    private static String SOURCE_ALARM = "SourceAlarm";
    private static String ENTITY_SUBDOMAIN_NAME = "Xphone:AdapterSiemens_nw";
    private static String ENTITY_ID_IN_SUBDOMAIN = "123";
    private static String ENTITY_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER = "NotificationIdentifier";

    private CommandExecutor commandExecutor;

    @Before
    public void init() {
        commandExecutor = new CommandExecutor();
    }

    @Test
    public void test_whenUnknownRequest_itIsFlagged() {
        // Note: ValueJoiner interface does not allow to ignore, i.e. a value must be produced.
        //  So, in order not to damage an entity value, we expect that a special value is produced.

        // Let's prepare a command which is unknown to our logic.
        // ( Note: We could define an additional schema, but for now as an incorrect command type we just use EntityV1 schema. )
        EntityV1 uknownRequest = EntityV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName("dummy")
                .setEntitySubdomainName("dummy")
                .setEntityIdInSubdomain("dummy")
                .build();

        // Dummy value.
        GenericRecord currentEntityValue = null;

        EntityV1 resultEntity = commandExecutor.apply(uknownRequest, currentEntityValue);

        assertSame(CommandExecutor.UKNOWN_ENTITY_TO_BE_IGNORED, resultEntity);
    }

    @Test
    public void test_whenCreateRequestForNotExisting_entityIsCreated() {

        // Let's prepare the input command.
        CreateEntityRequestV1 command = prepareCreateEntityRequest();

        // We provide null to indicate that this entity does not exist.
        GenericRecord currentEntityValue = null;

        EntityV1 resultEntity = commandExecutor.apply(command, currentEntityValue);

        // Verify.
        assertEquals(command.getEntityTypeName(), resultEntity.getEntityTypeName());
        assertEquals(command.getEntitySubdomainName(), resultEntity.getEntitySubdomainName());
        assertEquals(command.getEntityIdInSubdomain(), resultEntity.getEntityIdInSubdomain());
        assertEquals(command.getEntityAttributes(), resultEntity.getEntityAttributes());
    }

    @Test
    public void test_whenCreateRequestForExisting_itIsFlagged() {

        // Let's prepare the command request.
        CreateEntityRequestV1 command = prepareCreateEntityRequest();

        // We provide current value.
        GenericRecord currentEntityValue = prepareEntity();

        EntityV1 resultEntity = commandExecutor.apply(command, currentEntityValue);

        assertSame(CommandExecutor.ALREADY_EXISTING_ENTITY_TO_BE_IGNORED, resultEntity);
    }

    @Test
    public void test_whenDeleteRequestForExisting_entityIsDeleted() {

        // Let's prepare the command request.
        DeleteEntityRequestV1 command = prepareDeleteEntityRequest();

        // We provide current value.
        GenericRecord currentEntityValue = prepareEntity();

        EntityV1 resultEntity = commandExecutor.apply(command, currentEntityValue);

        // Value null is a tombstone. (i.e. a delete from a compacted kafka topic)
        assertSame(null, resultEntity);
    }

    @Test
    public void test_whenDeleteRequestForNotExisting_itIsFlagged() {

        // Let's prepare the command request.
        DeleteEntityRequestV1 command = prepareDeleteEntityRequest();

        // We provide null to indicate that this entity does not exist.
        GenericRecord currentEntityValue = null;

        EntityV1 resultEntity = commandExecutor.apply(command, currentEntityValue);

        assertSame(CommandExecutor.NOT_EXISTING_ENTITY_TO_BE_IGNORED, resultEntity);
    }

    private CreateEntityRequestV1 prepareCreateEntityRequest() {

        Map<CharSequence, CharSequence> attributes = new HashMap<>();
        attributes.put(ENTITY_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER, ENTITY_ID_IN_SUBDOMAIN);

        return CreateEntityRequestV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(SOURCE_ALARM)
                .setEntitySubdomainName(ENTITY_SUBDOMAIN_NAME)
                .setEntityIdInSubdomain(ENTITY_ID_IN_SUBDOMAIN)
                .setEntityAttributes(attributes)
                .build();
    }

    private DeleteEntityRequestV1 prepareDeleteEntityRequest() {

        return DeleteEntityRequestV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(SOURCE_ALARM)
                .setEntitySubdomainName(ENTITY_SUBDOMAIN_NAME)
                .setEntityIdInSubdomain(ENTITY_ID_IN_SUBDOMAIN)
                .build();
    }

    private EntityV1 prepareEntity() {

        Map<CharSequence, CharSequence> attributes = new HashMap<>();
        attributes.put(ENTITY_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER, ENTITY_ID_IN_SUBDOMAIN);

        return EntityV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(SOURCE_ALARM)
                .setEntitySubdomainName(ENTITY_SUBDOMAIN_NAME)
                .setEntityIdInSubdomain(ENTITY_ID_IN_SUBDOMAIN)
                .setEntityAttributes(attributes)
                .build();
    }

}
