package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommandProcessorTest {

    private static String SOURCE_ALARM = "SourceAlarm";
    private static String ENTITY_SUBDOMAIN_NAME = "Xphone:AdapterSiemens_nw";
    private static String ENTITY_ID_IN_SUBDOMAIN = "123";
    private static String ENTITY_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER = "NotificationIdentifier";

    private CommandProcessor commandProcessor;
    private KeyValueStore<String, EntityV1> entityKVStateStoreMock;
    private ProcessorContext processorContextMock;

    @Before
    public void init() {
        // Let's prepare Processor API stubs and mocks.
        entityKVStateStoreMock = Mockito.mock(KeyValueStore.class);
        processorContextMock = Mockito.mock(ProcessorContext.class);
        when(processorContextMock.getStateStore("dummy")).thenReturn(entityKVStateStoreMock);

        // Let's create tested object.
        commandProcessor = new CommandProcessor("dummy");
        commandProcessor.init(processorContextMock);
    }

    @Test
    public void test_whenUnknownRequest_itIsFlagged() {

        // Let's prepare a command which is unknown to our logic.
        // ( Note: We could define an additional schema, but for now as an incorrect command type we just use EntityV1 schema. )
        EntityV1 uknownRequest = EntityV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName("dummy")
                .setEntitySubdomainName("dummy")
                .setEntityIdInSubdomain("dummy")
                .build();

        commandProcessor.process("dummy", uknownRequest);

        // Nothing should be stored.
        verifyZeroInteractions(entityKVStateStoreMock);
        // Nothing should be forwarded.
        verify(processorContextMock).getStateStore(any());
        verifyNoMoreInteractions(processorContextMock);
    }

    @Test
    public void test_whenCreateRequestForNotExisting_entityIsCreated() {

        // Let's prepare the input command.
        CreateEntityRequestV1 command = prepareCreateEntityRequest();

        commandProcessor.process(ENTITY_ID_IN_SUBDOMAIN, command);

        // Let's verify that expected key and entity were stored in store.
        ArgumentCaptor<String> argumentKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EntityV1> argumentValue = ArgumentCaptor.forClass(EntityV1.class);
        verify(entityKVStateStoreMock).put(argumentKey.capture(), argumentValue.capture());
        EntityV1 resultEntity = argumentValue.getValue();
        //
        // Verify key.
        assertEquals(ENTITY_ID_IN_SUBDOMAIN, argumentKey.getValue());
        //
        // Verify entity content.
        assertEquals(command.getEntityTypeName(), resultEntity.getEntityTypeName());
        assertEquals(command.getEntitySubdomainName(), resultEntity.getEntitySubdomainName());
        assertEquals(command.getEntityIdInSubdomain(), resultEntity.getEntityIdInSubdomain());
        assertEquals(command.getEntityAttributes(), resultEntity.getEntityAttributes());

        // And verify that the same key and entity were forwarded.
        ArgumentCaptor<EntityV1> argumentForwarded = ArgumentCaptor.forClass(EntityV1.class);
        verify(processorContextMock).forward(argumentKey.capture(), argumentForwarded.capture());
        EntityV1 forwardedEntity = argumentForwarded.getValue();
        assertEquals(ENTITY_ID_IN_SUBDOMAIN, argumentKey.getValue());
        assertSame(resultEntity, forwardedEntity);
    }

    @Test
    public void test_whenCreateRequestForExisting_itIsIgnored() {

        // Let's prepare the command request.
        CreateEntityRequestV1 command = prepareCreateEntityRequest();
        // Let's fill the store mock with "existing" entity.
        when(entityKVStateStoreMock.get(ENTITY_ID_IN_SUBDOMAIN)).thenReturn( prepareEntity() );

        commandProcessor.process(ENTITY_ID_IN_SUBDOMAIN, command);

        // Nothing should be stored.
        verify(entityKVStateStoreMock).get(any());
        verifyZeroInteractions(entityKVStateStoreMock);
        // Nothing should be forwarded.
        verify(processorContextMock).getStateStore(any());
        verifyNoMoreInteractions(processorContextMock);
    }

    @Test
    public void test_whenDeleteRequestForExisting_entityIsDeleted() {

        // Let's prepare the command request.
        DeleteEntityRequestV1 command = prepareDeleteEntityRequest();
        // Let's fill the store mock with "existing" entity.
        when(entityKVStateStoreMock.get(ENTITY_ID_IN_SUBDOMAIN)).thenReturn( prepareEntity() );

        commandProcessor.process(ENTITY_ID_IN_SUBDOMAIN, command);

        // Let's verify that expected key was removed from the store.
        ArgumentCaptor<String> argumentKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EntityV1> argumentValue = ArgumentCaptor.forClass(EntityV1.class);
        verify(entityKVStateStoreMock).delete(ENTITY_ID_IN_SUBDOMAIN);

        // And verify that the same key was forwarded with null (a tombstone) value.
        ArgumentCaptor<EntityV1> argumentForwarded = ArgumentCaptor.forClass(EntityV1.class);
        verify(processorContextMock).forward(argumentKey.capture(), argumentForwarded.capture());
        EntityV1 forwardedEntity = argumentForwarded.getValue();
        assertEquals(ENTITY_ID_IN_SUBDOMAIN, argumentKey.getValue());
        assertSame(null, forwardedEntity);
    }

    @Test
    public void test_whenDeleteRequestForNotExisting_itIsIgnored() {

        // Let's prepare the command request.
        DeleteEntityRequestV1 command = prepareDeleteEntityRequest();

        commandProcessor.process(ENTITY_ID_IN_SUBDOMAIN, command);

        // Nothing should be stored.
        verify(entityKVStateStoreMock).get(any());
        verifyZeroInteractions(entityKVStateStoreMock);
        // Nothing should be forwarded.
        verify(processorContextMock).getStateStore(any());
        verifyNoMoreInteractions(processorContextMock);
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
