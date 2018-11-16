package com.j9soft.saas.alarms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j9soft.saas.alarms.dao.DaoRequestBuilder;
import com.j9soft.saas.alarms.dao.RequestDao;

public class ResyncAllAlarmsEndRequestDto extends RequestDto {

    @JsonIgnore
    private ResyncAllEndSubdomainRequestV1 requestDao;

    @Override
    public void saveInDao(RequestDao requestDao, RequestDao.Callback callback) {
        requestDao.saveNewRequest(this.requestDao, callback);
    }

    @Override
    public void buildDaoRequest(DaoRequestBuilder daoRequestBuilder) {

        requestDao = daoRequestBuilder.buildResyncAllEnd();
    }
}
