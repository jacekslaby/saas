package com.j9soft.saas.alarms.testdata;

import com.j9soft.saas.alarms.dao.DaoRequestBuilder;
import com.j9soft.saas.alarms.model.*;

/**
 * Pool of DTO requests used in unit tests.
 */
public class TestDtoRequests {

    private CreateAlarmDto createAlarmDto;
    private DeleteAlarmDto deleteAlarmDto;
    private ResyncAllStartDto resyncAllStartDto;
    private ResyncAllEndDto resyncAllEndDto;

    private TestDtoRequestsContents dtoRequestsContents;

    public static TestDtoRequests newBuilder(DaoRequestBuilder builder) {
        TestDtoRequests result = new TestDtoRequests();

        result.dtoRequestsContents = TestDtoRequestsContents.newBuilder();

        result.createAlarmDto = new CreateAlarmDto();
        result.createAlarmDto.setRequestContent(result.dtoRequestsContents.getCreateAlarm());
        result.createAlarmDto.buildDaoRequest(builder);

        result.deleteAlarmDto = new DeleteAlarmDto();
        result.deleteAlarmDto.setRequestContent(result.dtoRequestsContents.getDeleteAlarm());
        result.deleteAlarmDto.buildDaoRequest(builder);

        result.resyncAllStartDto = new ResyncAllStartDto();
        result.resyncAllStartDto.buildDaoRequest(builder);

        result.resyncAllEndDto = new ResyncAllEndDto();
        result.resyncAllEndDto.buildDaoRequest(builder);

        return result;
    }

    public CreateAlarmDto getCreateAlarmDto() {
        return createAlarmDto;
    }

    public DeleteAlarmDto getDeleteAlarmDto() {
        return deleteAlarmDto;
    }

    public ResyncAllStartDto getResyncAllStartDto() {
        return resyncAllStartDto;
    }

    public ResyncAllEndDto getResyncAllEndDto() {
        return resyncAllEndDto;
    }
}
