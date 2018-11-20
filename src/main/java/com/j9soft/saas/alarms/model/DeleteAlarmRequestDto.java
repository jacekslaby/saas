package com.j9soft.saas.alarms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.saas.alarms.dao.DaoRequestBuilder;
import com.j9soft.saas.alarms.dao.RequestDao;
import org.openapitools.client.model.DeleteAlarmRequest;

public class DeleteAlarmRequestDto extends RequestDto {

    @JsonProperty("request_content")
    private DeleteAlarmRequest requestContent;

    @JsonIgnore
    private DeleteEntityRequestV1 krepositoryCommand;

    @Override
    public void saveInDao(RequestDao requestDao, RequestDao.Callback callback) {
        requestDao.saveNewRequest(krepositoryCommand, callback);
    }

    @Override
    public void buildDaoRequest(DaoRequestBuilder daoRequestBuilder) {

        krepositoryCommand = daoRequestBuilder.buildDeleteEntityRequest(this.requestContent);
    }

    public void setRequestContent(DeleteAlarmRequest requestContent) {
        this.requestContent = requestContent;
    }
}

