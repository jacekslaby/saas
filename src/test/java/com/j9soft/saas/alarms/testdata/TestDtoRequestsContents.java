package com.j9soft.saas.alarms.testdata;

import org.openapitools.client.model.*;

import java.time.OffsetDateTime;

/**
 * Pool of contents of DTO requests used in unit tests.
 */
public class TestDtoRequestsContents {
    private static final String ALARM_NOID = "eric2g:341";

    private CreateAlarmRequest createAlarmRequest;
    private DeleteAlarmRequest deleteAlarmRequest;

    public CreateAlarmRequest getCreateAlarmRequest() {
        return createAlarmRequest;
    }

    public DeleteAlarmRequest getDeleteAlarmRequest() {
        return deleteAlarmRequest;
    }

    public static TestDtoRequestsContents newBuilder() {
        TestDtoRequestsContents result = new TestDtoRequestsContents();
        
        // Let's prepare our test input request in JSON.
        String eventTimeString = "2018-10-19T13:44:56.334+02:00";
        long eventTime = OffsetDateTime.parse(eventTimeString).toInstant().toEpochMilli();

        // Let's create CreateAlarmRequest.
        AlarmDTOAdditionalProperties alarmDTOAdditionalPropertiesForApi = new AlarmDTOAdditionalProperties();
        alarmDTOAdditionalPropertiesForApi.put("additional_text", "Detailed information");
        alarmDTOAdditionalPropertiesForApi.put("managed_object_instance", "BTS:333");
        AlarmDTO alarmDto = new AlarmDTO()
                .notificationIdentifier(ALARM_NOID)
                .eventTime(eventTimeString)
                .perceivedSeverity(1)
                .additionalProperties(alarmDTOAdditionalPropertiesForApi);
        result.createAlarmRequest = new CreateAlarmRequest()
                .alarmDto(alarmDto);

        // Let's create DeleteAlarmRequest.
        DeletedAlarmDTO deletedAlarmDto = new DeletedAlarmDTO()
                .notificationIdentifier(ALARM_NOID)
                .eventTime(eventTimeString);
        result.deleteAlarmRequest = new DeleteAlarmRequest()
                .alarmDto(deletedAlarmDto);

        return result;
    }

}
