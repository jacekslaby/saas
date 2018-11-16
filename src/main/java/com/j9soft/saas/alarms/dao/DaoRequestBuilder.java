package com.j9soft.saas.alarms.dao;

import com.j9soft.saas.alarms.model.CreateEntityRequestV1;
import com.j9soft.saas.alarms.model.DeleteEntityRequestV1;
import com.j9soft.saas.alarms.model.ResyncAllEndSubdomainRequestV1;
import com.j9soft.saas.alarms.model.ResyncAllStartSubdomainRequestV1;
import org.openapitools.client.model.CreateAlarm;
import org.openapitools.client.model.DeleteAlarm;

/**
 * Interface implemented by objects which know how to convert a request from the OpenAPI model (e.g. a CreateAlarm)
 *  to a request from the DAO model (e.g. a CreateEntityRequestV1).
 */
public interface DaoRequestBuilder {
    /**
     * Visit a RequestDto containing CreateAlarm and build CreateEntityRequestV1 for it.
     */
    CreateEntityRequestV1 buildCreateEntityRequest(CreateAlarm createAlarm);

    /**
     * Visit a RequestDto containing ResyncAllStart and build ResyncAllStartSubdomainRequestV1 for it.
     */
    ResyncAllStartSubdomainRequestV1 buildResyncAllStart();

    /**
     * Visit a RequestDto containing ResyncAllEnd and build ResyncAllEndSubdomainRequestV1 for it.
     */
    public ResyncAllEndSubdomainRequestV1 buildResyncAllEnd();

    /**
     * Visit a RequestDto containing DeleteAlarm and build DeleteEntityRequestV1 for it.
     */
    public DeleteEntityRequestV1 buildDeleteEntityRequest(DeleteAlarm deleteAlarm);
}

