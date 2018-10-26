package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.model.CreateEntityRequest;

/**
 * Interface defining what functionality is provided by a data access layer.
 */
public interface SaasDao {

    /**
     * Create a new request of type CreateEntityRequest(ed).
     */
    void createRequest(CreateEntityRequest request);

}
