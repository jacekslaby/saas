package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.model.CreateEntityRequest;
import com.j9soft.saas.alarms.model.DeleteEntityRequestV1;

/**
 * Interface defining what functionality is provided by a data access layer.
 */
public interface SaasDao {

    /**
     * Create a new request of type CreateEntityRequest(ed).
     */
    void createRequest(CreateEntityRequest request);

    /**
     * Create a new request of type DeleteEntityRequest(ed).
     */
    void createRequest(DeleteEntityRequestV1 request);

}
