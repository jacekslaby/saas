package com.j9soft.saas.alarms.service;

import com.j9soft.saas.alarms.model.RequestDto;

/**
 * Objects with this interface are used by Saas service (SaasV1Service)
 * as an intermediate layer to DAO. (SaasDao)
 */
public interface SaasPublisher {

    /**
     * Publish a new request. (e.g. of type DeleteEntityRequestV1(ed), CreateEntityRequestV1(ed)).
     * It is an atomic change. (i.e. all or nothing)
     */
    void publishRequest(PublishTask publishTask, RequestDto requestDto);

    /**
     * Publish new requests. (e.g. of type DeleteEntityRequestV1(ed), CreateEntityRequestV1(ed)).
     * It is NOT an atomic change. (i.e. some requests may come through and some may fail)
     */
    void publishRequestsWithArray(PublishTask publishTask, RequestDto[] requestsArray);

    /**
     * Creates a new object which gathers results of calls to publishRequest / publishRequestsWithArray.
     * @return
     */
    PublishTask createNewTask();

}
