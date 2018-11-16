package com.j9soft.saas.alarms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j9soft.saas.alarms.dao.DaoRequestBuilderV1;
import com.j9soft.saas.alarms.model.RequestDto;
import com.j9soft.saas.alarms.model.RequestsListDto;
import org.openapitools.client.model.MultiStatusResponse;
import org.openapitools.client.model.RequestCreatedResponse;
import org.openapitools.client.model.RequestCreationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaasV1Service {

    // @Autowired  - is not used because:
    // https://spring.io/blog/2016/04/15/testing-improvements-in-spring-boot-1-4
    // "Don’t use field injection as it just makes your tests harder to write."
    //
    private final SaasPublisher saasPublisher;

    private ObjectMapper mapper;

    @Autowired
    public SaasV1Service(SaasPublisher saasPublisher) {

        this.saasPublisher = saasPublisher;

        // For performance reasons we should share ObjectMapper instance.
        // ( https://stackoverflow.com/questions/3907929/should-i-declare-jacksons-objectmapper-as-a-static-field )
        mapper = new ObjectMapper();
    }

    public RequestCreatedResponse createRequest(String domainName, String adapterName, RequestDto requestDto) {

        // Prepare a DAO layer request.
        DaoRequestBuilderV1 builder = DaoRequestBuilderV1.newBuilder(domainName, adapterName);
        requestDto.buildDaoRequest(builder);

        // Publish, i.e. save the request.
        PublishTask publishTask = saasPublisher.createNewTask();
        saasPublisher.publishRequest(publishTask, requestDto);

        // Blocking wait for results.
        Exception[] results;
        try {
            results = publishTask.getResults();
        } catch (InterruptedException e) {
            // Something went wrong. We need to return '500' HTTP status code.
            throw new RuntimeException("Unexpected error. The request could not have been created. Please re-try later.");
        }

        // Build response.
        if (results[0] == null) {
            // RequestDto was created successfully in Dao.
            RequestCreatedResponse requestCreatedResponse = new RequestCreatedResponse();
            // @TODO return UUID requestCreatedResponse.setUuid(request. wrappedRequest.getUUID() );
            return requestCreatedResponse;
        } else {
            // Something went wrong. We need to return '500' HTTP status code.
            throw new RuntimeException("Unexpected error. The request could not have been created. Please re-try later.");
        }
    }

    public MultiStatusResponse createRequestsWithList(String domainName, String adapterName, RequestsListDto requestsListDto) {

        // Prepare DAO layer requests.
        DaoRequestBuilderV1 builder = DaoRequestBuilderV1.newBuilder(domainName, adapterName);
        RequestDto[] requestsArray = requestsListDto.getRequests();
        for (RequestDto requestDto: requestsArray) {
            requestDto.buildDaoRequest(builder);
        }

        // Publish, i.e. save the requests.
        PublishTask publishTask = saasPublisher.createNewTask();
        saasPublisher.publishRequestsWithArray(publishTask, requestsArray);

        // Blocking wait for results.
        Exception[] results;
        try {
            results = publishTask.getResults();
        } catch (InterruptedException e) {
            // Something went wrong. We need to return '500' HTTP status code.
            throw new RuntimeException("Unexpected error. The request could not have been created. Please re-try later.");
        }

        // Build response.
        MultiStatusResponse response = new MultiStatusResponse();
        RequestCreationResult[] requestResults = new RequestCreationResult[results.length];
        for (int i = 0; i < results.length; i++) {
            requestResults[i] = new RequestCreationResult();
            if (results[i] != null) {
                requestResults[i].setStatus(RequestCreationResult.StatusEnum._500);
            } else {
                requestResults[i].setStatus(RequestCreationResult.StatusEnum._200);
                //@TODO add UUID to returned RequestResult
            }
        }
        return response;
    }
}
