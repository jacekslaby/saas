package com.j9soft.saas.alarms.testdata;

import com.j9soft.saas.alarms.dao.DaoRequestBuilder;
import com.j9soft.saas.alarms.model.*;

/**
 * Pool of DTO requests used in unit tests.
 */
public class TestDtoRequests {

    private CreateAlarmRequestDto createAlarmRequestDto;
    private DeleteAlarmRequestDto deleteAlarmRequestDto;
    private ResyncAllAlarmsStartRequestDto resyncAllAlarmsStartRequestDto;
    private ResyncAllAlarmsEndRequestDto resyncAllAlarmsEndRequestDto;

    private TestDtoRequestsContents dtoRequestsContents;

    public static TestDtoRequests newBuilder(DaoRequestBuilder builder) {
        TestDtoRequests result = new TestDtoRequests();

        result.dtoRequestsContents = TestDtoRequestsContents.newBuilder();

        result.createAlarmRequestDto = new CreateAlarmRequestDto();
        result.createAlarmRequestDto.setRequestContent(result.dtoRequestsContents.getCreateAlarmRequest());
        result.createAlarmRequestDto.buildDaoRequest(builder);

        result.deleteAlarmRequestDto = new DeleteAlarmRequestDto();
        result.deleteAlarmRequestDto.setRequestContent(result.dtoRequestsContents.getDeleteAlarmRequest());
        result.deleteAlarmRequestDto.buildDaoRequest(builder);

        result.resyncAllAlarmsStartRequestDto = new ResyncAllAlarmsStartRequestDto();
        result.resyncAllAlarmsStartRequestDto.buildDaoRequest(builder);

        result.resyncAllAlarmsEndRequestDto = new ResyncAllAlarmsEndRequestDto();
        result.resyncAllAlarmsEndRequestDto.buildDaoRequest(builder);

        return result;
    }

    public CreateAlarmRequestDto getCreateAlarmRequestDto() {
        return createAlarmRequestDto;
    }

    public DeleteAlarmRequestDto getDeleteAlarmRequestDto() {
        return deleteAlarmRequestDto;
    }

    public ResyncAllAlarmsStartRequestDto getResyncAllAlarmsStartRequestDto() {
        return resyncAllAlarmsStartRequestDto;
    }

    public ResyncAllAlarmsEndRequestDto getResyncAllAlarmsEndRequestDto() {
        return resyncAllAlarmsEndRequestDto;
    }
}
