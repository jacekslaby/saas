package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.model.RequestDto;
import com.j9soft.saas.alarms.model.RequestsListDto;
import org.openapitools.model.MultiResultResponse;
import org.openapitools.model.RequestCreatedResponse;

/**
 * Interface defining functionality provided by API (version 1) of Source Active Alarms Store.
 *
 * More details about API can be found in file src/doc/openapi.yaml.
 */
public interface SaasV1 {

    /**
     * Create a new RequestDto.
     */
    RequestCreatedResponse createRequest(String domainName, String adapterName, RequestDto requestDto);

    /**
     * Create several Requests.
     */
    MultiResultResponse createRequestsWithList(String domainName, String adapterName, RequestsListDto requestsListDto);

}
