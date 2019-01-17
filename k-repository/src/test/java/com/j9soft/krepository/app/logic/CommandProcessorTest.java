package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.EntityAttributes;
import com.j9soft.krepository.v1.entitiesmodel.Attributes;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1FieldNames;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommandProcessorTest {

    private static String SOURCE_ALARM = "SourceAlarm";
    private static String ENTITY_SUBDOMAIN_NAME = "Xphone:AdapterSiemens_nw";
    private static String ENTITY_ID_IN_SUBDOMAIN = "123";

    private CommandProcessor commandProcessor;
    private KeyValueStore<String, GenericRecord> entityKVStateStoreMock;
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
        ArgumentCaptor<GenericRecord> argumentValue = ArgumentCaptor.forClass(GenericRecord.class);
        verify(entityKVStateStoreMock).put(argumentKey.capture(), argumentValue.capture());
        GenericRecord resultEntity = argumentValue.getValue();
        //
        // Verify key.
        assertEquals(ENTITY_ID_IN_SUBDOMAIN, argumentKey.getValue());
        //
        // Verify entity content.
        assertEquals(command.getEntityTypeName(), resultEntity.get(EntityV1FieldNames.ENTITY_TYPE_NAME) );
        assertEquals(command.getEntitySubdomainName(), resultEntity.get(EntityV1FieldNames.ENTITY_SUBDOMAIN_NAME) );
        assertEquals(command.getEntityIdInSubdomain(), resultEntity.get(EntityV1FieldNames.ENTITY_ID_IN_SUBDOMAIN));
        assertAttributesAreEqual(command.getEntityAttributes(), (GenericRecord) resultEntity.get(EntityV1FieldNames.ATTRIBUTES));

        // And verify that the same key and entity were forwarded.
        ArgumentCaptor<GenericRecord> argumentForwarded = ArgumentCaptor.forClass(GenericRecord.class);
        verify(processorContextMock).forward(argumentKey.capture(), argumentForwarded.capture());
        GenericRecord forwardedEntity = argumentForwarded.getValue();
        assertEquals(ENTITY_ID_IN_SUBDOMAIN, argumentKey.getValue());
        assertSame(resultEntity, forwardedEntity);
    }

    private void assertAttributesAreEqual(EntityAttributes expected, GenericRecord actual) {
        for (Schema.Field fieldExpected: expected.getSchema().getFields()) {
            String fieldName = fieldExpected.name();
            assertEquals("values are not equal in entity field named '" + fieldName + "'",
                    expected.get(fieldName), actual.get(fieldName));
        }
        // We also want to detect when there are more fields in the actual than in the expected.
        for (Schema.Field fieldActual: actual.getSchema().getFields()) {
            String fieldName = fieldActual.name();
            assertEquals("values are not equal in entity field named '" + fieldName + "'",
                    expected.get(fieldName), actual.get(fieldName));
        }
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
        verify(entityKVStateStoreMock).approximateNumEntries();  // @TODO how to do it better ?
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
        verify(entityKVStateStoreMock).approximateNumEntries();  // @TODO how to do it better ?
        verifyZeroInteractions(entityKVStateStoreMock);
        // Nothing should be forwarded.
        verify(processorContextMock).getStateStore(any());
        verifyNoMoreInteractions(processorContextMock);
    }

    private CreateEntityRequestV1 prepareCreateEntityRequest() {

        EntityAttributes entityAttributes = EntityAttributes.newBuilder()
                .setNotificationIdentifier(ENTITY_ID_IN_SUBDOMAIN)
                .build();

        return CreateEntityRequestV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(SOURCE_ALARM)
                .setEntitySubdomainName(ENTITY_SUBDOMAIN_NAME)
                .setEntityIdInSubdomain(ENTITY_ID_IN_SUBDOMAIN)
                .setEntityAttributes(entityAttributes)
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

        Attributes attributes = Attributes.newBuilder()
                .setNotificationIdentifier(ENTITY_ID_IN_SUBDOMAIN)
                .build();

        return EntityV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(SOURCE_ALARM)
                .setEntitySubdomainName(ENTITY_SUBDOMAIN_NAME)
                .setEntityIdInSubdomain(ENTITY_ID_IN_SUBDOMAIN)
                .setAttributes(attributes)
                .build();
    }

}
