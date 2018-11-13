package com.j9soft.saas.alarms;

import org.openapitools.client.model.MultiStatusResponse;
import org.openapitools.client.model.RequestCreatedResponse;

/**
 * Interface defining functionality provided by API (version 1) of Source Active Alarms Store.
 *
 * More details about API can be found in file src/doc/openapi.yaml.
 */
public interface SaasV1 {

    /**
     * Create a new Request.
     */
    RequestCreatedResponse createRequest(String domainName, String adapterName, String requestDTOAsJson);

    /**
     * Create several Requests.
     */
    MultiStatusResponse createRequestsWithList(String domainName, String adapterName, String requestDTOArrayAsJson);

}
