package com.j9soft.saas.alarms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j9soft.saas.alarms.model.CreateEntityRequest;
import com.j9soft.saas.alarms.model.Definitions;
import io.swagger.model.AlarmDTO;
import io.swagger.model.AlarmDTOAdditionalProperties;
import io.swagger.model.CreateAlarm;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;

/*
 * I assume it is enough to test controller methods without testing HTTP wiring. (i.e. without TestRestTemplate, etc.)
 * (See also: https://spring.io/guides/gs/spring-boot/  @Autowired private TestRestTemplate template;  )
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasV1ControllerTest {

    private static final String DOMAIN = "controllerTest";
    private static final String ADAPTER_NAME = "controllerTestAdapter";

    private static final String ALARM_NOID = "eric2g:341";
    private static final String ALARM_JSON = "{\"severity\"=\"1\"}";
    private static final String REQUEST_JSON = "{\"request_type\":\"CreateAlarm\"}";

    private SaasV1 saas;
    private SaasDao saasDaoMock;

    @Before
    public void initRaas() {

        // Let's register what should be returned.
        //
        saasDaoMock = Mockito.mock(SaasDao.class);

        // Let's create the tested bean.
        saas = new SaasV1Controller(this.saasDaoMock, new SaasV1Service()); // @TODO think about encapsulated autowire for SaasV1Service
    }

    @Test
    public void whenPostedCreateAlarmRequest_itIsSavedToDao() throws JsonProcessingException {

        // Let's prepare our test input request in JSON.
        String eventTimeString = "2018-10-19T13:44:56.334+02:00";
        long eventTime = OffsetDateTime.parse(eventTimeString).toInstant().toEpochMilli();
        AlarmDTOAdditionalProperties alarmDTOAdditionalPropertiesForApi = new AlarmDTOAdditionalProperties();
        alarmDTOAdditionalPropertiesForApi.put("additional_text", "Detailed information");
        alarmDTOAdditionalPropertiesForApi.put("managed_object_instance", "BTS:333");
        AlarmDTO alarmDtoForApi = new AlarmDTO()
                .notificationIdentifier(ALARM_NOID)
                .eventTime(eventTimeString)
                .perceivedSeverity(1)
                .additionalProperties(alarmDTOAdditionalPropertiesForApi);
        CreateAlarm createAlarmRequest = new CreateAlarm()
                .requestType(CreateAlarm.RequestTypeEnum.CREATEALARM)
                .alarmDto(alarmDtoForApi);
        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(createAlarmRequest);

        // Let's prepare the expected request. (i.e. expected to be generated and saved in Dao)
        Map<CharSequence,CharSequence> alarmDtoForDao = new HashMap<>();
        alarmDtoForDao.put(Definitions.ALARM_ATTRIBUTE_NAME__EVENT_TIME, eventTimeString);
        alarmDtoForDao.put(Definitions.ALARM_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER, ALARM_NOID);
        alarmDtoForDao.put(Definitions.ALARM_ATTRIBUTE_NAME__PERCEIVED_SEVERITY, "1");
        alarmDtoForDao.putAll(alarmDTOAdditionalPropertiesForApi);
        CreateEntityRequest request = CreateEntityRequest.newBuilder()
                .setUuid("foo")  // we expect it to be overwritten  (it is required in DAO schema so we must provide it here)
                .setEntryDate(1)  // we expect it to be overwritten  (it is required in DAO schema so we must provide it here)
                .setEntityTypeName(Definitions.ALARM_ENTITY_TYPE_NAME)
                .setEntityDomainName(DOMAIN)
                .setEntitySubdomainName(ADAPTER_NAME)
                .setEntityIdInSubdomain(ALARM_NOID)
                .setEntityAttributes(alarmDtoForDao)
                .setEventDate(eventTime)
                .setLineageStartDate(null)
                .build();

        // Let's post a create request.
        saas.createRequest(DOMAIN, ADAPTER_NAME, requestJson);

        // Let's verify that it was saved in Dao:
        //
        // - some fields should be equal:
        verify(saasDaoMock).createRequest(refEq(request,
                Definitions.DAO_SCHEMA_REQUEST__UUID, Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE)); // fields excluded from comparison
        //
        // - other fields should be auto-generated
        ArgumentCaptor<CreateEntityRequest> argument = ArgumentCaptor.forClass(CreateEntityRequest.class);
        verify(saasDaoMock).createRequest(argument.capture());
        assertTrue("proper uuid should be generated", UUID.fromString(argument.getValue().getUuid().toString()).version() > 0);
        assertThat(Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE,
                argument.getValue().getEntryDate(), lessThanOrEqualTo(System.currentTimeMillis()));
    }

    @Test
    public void whenPostedDeleteAlarmRequest_itIsSavedToDao() {

        fail("TODO");
    }

    @Test
    public void whenPostedBunchOfRequests_theyAreSavedToDao() {

        fail("TODO");
    }

    @Test
    public void whenPostedResyncRequests_theyAreSavedToDao() {

        fail("TODO");
    }

}