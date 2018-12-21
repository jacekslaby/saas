package com.j9soft.saas.alarms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.saas.alarms.dao.DaoRequestBuilder;
import com.j9soft.saas.alarms.dao.RequestDao;
import org.openapitools.model.DeleteAlarmRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DeleteAlarmRequestDto extends RequestDto {

    @NotNull
    @Valid
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

    @Override
    public CharSequence getDaoRequestUuid() {
        return krepositoryCommand.getUuid();
    }

    public void setRequestContent(DeleteAlarmRequest requestContent) {
        this.requestContent = requestContent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeleteAlarmRequestDto {\n");

        sb.append("    requestContent: ").append(toIndentedString(requestContent)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

