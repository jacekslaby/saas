package com.j9soft.saas.alarms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j9soft.saas.alarms.model.CreateEntityRequest;
import com.j9soft.saas.alarms.model.Definitions;
import io.swagger.model.AlarmDTO;
import io.swagger.model.AlarmDTOAdditionalProperties;
import io.swagger.model.CreateAlarm;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

class TestCreateEntityRequest {
    public static final String DOMAIN = "controllerTest";
    public static final String ADAPTER_NAME = "controllerTestAdapter";

    private static final String ALARM_NOID = "eric2g:341";

    private String requestJson;
    private CreateEntityRequest request;

    public String getRequestJson() {
        return requestJson;
    }

    public CreateEntityRequest getRequestObject() {
        return request;
    }

    public String getDomain() { return DOMAIN; }

    public String getAdapterName() { return ADAPTER_NAME; }

    public TestCreateEntityRequest build() throws JsonProcessingException {
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
        requestJson = mapper.writeValueAsString(createAlarmRequest);

        // Let's prepare the expected request. (i.e. expected to be generated and saved in Dao)
        Map<CharSequence,CharSequence> alarmDtoForDao = new HashMap<>();
        alarmDtoForDao.put(Definitions.ALARM_ATTRIBUTE_NAME__EVENT_TIME, eventTimeString);
        alarmDtoForDao.put(Definitions.ALARM_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER, ALARM_NOID);
        alarmDtoForDao.put(Definitions.ALARM_ATTRIBUTE_NAME__PERCEIVED_SEVERITY, "1");
        alarmDtoForDao.putAll(alarmDTOAdditionalPropertiesForApi);
        request = CreateEntityRequest.newBuilder()
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
        return this;
    }
}
