package com.j9soft.saas.alarms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllEndSubdomainRequestV1;
import com.j9soft.saas.alarms.dao.DaoRequestBuilder;
import com.j9soft.saas.alarms.dao.RequestDao;

public class ResyncAllAlarmsEndRequestDto extends RequestDto {

    @JsonIgnore
    private ResyncAllEndSubdomainRequestV1 krepositoryCommand;

    @Override
    public void saveInDao(RequestDao requestDao, RequestDao.Callback callback) {
        requestDao.saveNewRequest(krepositoryCommand, callback);
    }

    @Override
    public void buildDaoRequest(DaoRequestBuilder daoRequestBuilder) {

        krepositoryCommand = daoRequestBuilder.buildResyncAllEnd();
    }

    @Override
    public CharSequence getDaoRequestUuid() {
        return krepositoryCommand.getUuid();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResyncAllAlarmsEndRequestDto {\n");
        sb.append("}");
        return sb.toString();
    }
}
