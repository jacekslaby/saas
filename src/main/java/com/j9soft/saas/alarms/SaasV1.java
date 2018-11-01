package com.j9soft.saas.alarms;

/**
 * Interface defining functionality provided by API (version 1) of Source Active Alarms Store.
 *
 * More details about API can be found in file src/doc/openapi.yaml.
 */
public interface SaasV1 {

    /**
     * Create a new Request.
     */
    void createRequest(String domainName, String adapterName, String requestDTOAsJson);

    /**
     * Create several Requests.
     */
    void createRequestsWithList(String domainName, String adapterName, String requestDTOArrayAsJson);

}
