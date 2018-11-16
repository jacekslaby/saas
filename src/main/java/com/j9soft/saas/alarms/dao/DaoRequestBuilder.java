package com.j9soft.saas.alarms.dao;

import com.j9soft.saas.alarms.model.CreateEntityRequestV1;
import com.j9soft.saas.alarms.model.DeleteEntityRequestV1;
import com.j9soft.saas.alarms.model.ResyncAllEndSubdomainRequestV1;
import com.j9soft.saas.alarms.model.ResyncAllStartSubdomainRequestV1;
import org.openapitools.client.model.CreateAlarmRequest;
import org.openapitools.client.model.DeleteAlarmRequest;

/**
 * Interface implemented by objects which know how to convert a request from the OpenAPI model (e.g. a CreateAlarm)
 *  to a request from the DAO model (e.g. a CreateEntityRequestV1).
 */
public interface DaoRequestBuilder {
    /**
     * Visit a RequestDto containing CreateAlarmRequest and build CreateEntityRequestV1 for it.
     */
    CreateEntityRequestV1 buildCreateEntityRequest(CreateAlarmRequest createAlarmRequest);

    /**
     * Visit a RequestDto containing ResyncAllAlarmsStartRequest and build ResyncAllStartSubdomainRequestV1 for it.
     */
    ResyncAllStartSubdomainRequestV1 buildResyncAllStart();

    /**
     * Visit a RequestDto containing ResyncAllAlarmsEndRequest and build ResyncAllEndSubdomainRequestV1 for it.
     */
    public ResyncAllEndSubdomainRequestV1 buildResyncAllEnd();

    /**
     * Visit a RequestDto containing DeleteAlarmRequest and build DeleteEntityRequestV1 for it.
     */
    public DeleteEntityRequestV1 buildDeleteEntityRequest(DeleteAlarmRequest deleteAlarmRequest);
}

