package com.j9soft.saas.alarms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j9soft.saas.alarms.dao.DaoRequestBuilder;
import com.j9soft.saas.alarms.dao.RequestDao;
import org.openapitools.client.model.DeleteAlarmRequest;

public class DeleteAlarmRequestDto extends RequestDto {

    @JsonProperty("request_content")
    private DeleteAlarmRequest requestContent;

    @JsonIgnore
    private DeleteEntityRequestV1 requestDao;

    @Override
    public void saveInDao(RequestDao requestDao, RequestDao.Callback callback) {
        requestDao.saveNewRequest(this.requestDao, callback);
    }

    @Override
    public void buildDaoRequest(DaoRequestBuilder daoRequestBuilder) {

        requestDao = daoRequestBuilder.buildDeleteEntityRequest(this.requestContent);
    }

    public void setRequestContent(DeleteAlarmRequest requestContent) {
        this.requestContent = requestContent;
    }
}

