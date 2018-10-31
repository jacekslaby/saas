
package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.model.CreateEntityRequestV1;
import com.j9soft.saas.alarms.model.DeleteEntityRequestV1;
import com.j9soft.saas.alarms.model.ResyncAllEndSubdomainRequestV1;
import com.j9soft.saas.alarms.model.ResyncAllStartSubdomainRequestV1;

/**
 * Interface defining what functionality is provided by a data access layer.
 *
 * It is also a Visitor (https://en.wikipedia.org/wiki/Visitor_pattern)
 *  to SaasPublisher.Requests.
 * During a visit (i.e. when a Request accepts a SaasDao in SaasPublisher.Request.accept(SaasDao))
 *  an appropriate method of SaasDao is invoked. (e.g. #publishRequest(CreateEntityRequestV1 request)
 */
public interface SaasDao {

    /**
     * Create a new request of type CreateEntityRequest(ed).
     */
    void createRequest(CreateEntityRequestV1 request);

    /**
     * Create a new request of type DeleteEntityRequest(ed).
     */
    void createRequest(DeleteEntityRequestV1 request);

    /**
     * Create a new request of type ResyncAllStartSubdomainRequest(ed).
     */
    void createRequest(ResyncAllStartSubdomainRequestV1 request);

    /**
     * Create a new request of type ResyncAllEndSubdomainRequest(ed).
     */
    void createRequest(ResyncAllEndSubdomainRequestV1 request);

}
