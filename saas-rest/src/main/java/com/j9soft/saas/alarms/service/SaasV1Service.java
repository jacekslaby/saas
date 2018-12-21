package com.j9soft.saas.alarms.service;

import com.j9soft.saas.alarms.dao.DaoRequestBuilderV1;
import com.j9soft.saas.alarms.model.RequestDto;
import com.j9soft.saas.alarms.model.RequestsListDto;
import org.openapitools.model.MultiResultResponse;
import org.openapitools.model.RequestCreatedResponse;
import org.openapitools.model.RequestCreationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaasV1Service {

    private static final Logger logger = LoggerFactory.getLogger(SaasV1Service.class);

    // @Autowired  - is not used because:
    // https://spring.io/blog/2016/04/15/testing-improvements-in-spring-boot-1-4
    // "Don’t use field injection as it just makes your tests harder to write."
    //
    private final SaasPublisher saasPublisher;

    @Autowired
    public SaasV1Service(SaasPublisher saasPublisher) {

        this.saasPublisher = saasPublisher;
    }

    public RequestCreatedResponse createRequest(String domainName, String adapterName, RequestDto requestDto) {

        // Prepare a DAO layer request.
        DaoRequestBuilderV1 builder = DaoRequestBuilderV1.newBuilder(domainName, adapterName);
        requestDto.buildDaoRequest(builder);

        if (logger.isInfoEnabled()) {
            // We need entire JSON to be able to easily associate content to UUID in log output,
            //  however we do not want JSON to be in new lines, so we remove newline characters.
            //
            String requestDtoAsString = requestDto.toString().replace("\n", "");
            logger.info("RequestUuid:{} - createRequest(domainName='{}', adapterName='{}') - requestDto:{}",
                    requestDto.getDaoRequestUuid(), domainName, adapterName, requestDtoAsString);
        }

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

        // TODO AsyncResponse ?  (to improve performance/parallelizm ?)
        //
        // Build response.
        if (results[0] == null) {
            // RequestDto was created successfully in Dao.
            RequestCreatedResponse requestCreatedResponse = new RequestCreatedResponse();
            // @TODO return UUID requestCreatedResponse.setUuid(request. wrappedRequest.getUUID() );

            logger.info("RequestUuid:{} - createRequest - success", requestDto.getDaoRequestUuid());

            return requestCreatedResponse;
        } else {
            // Something went wrong. We need to return '500' HTTP status code.
            throw new RuntimeException("Unexpected error. The request could not have been created. Please re-try later.");
        }
    }

    public MultiResultResponse createRequestsWithList(String domainName, String adapterName, RequestsListDto requestsListDto) {

        if (logger.isInfoEnabled()) {
            // We need entire JSON to be able to easily associate content to UUID in log output,
            //  however we do not want JSON to be in new lines, so we remove newline characters.
            //
            String requestsListDtoAsString = requestsListDto.toString().replace("\n", "");
            logger.info("createRequestsWithList(domainName='{}', adapterName='{}') - requestsListDto:\n{}",
                    domainName, adapterName, requestsListDtoAsString);
        }

        // Prepare DAO layer requests.
        DaoRequestBuilderV1 builder = DaoRequestBuilderV1.newBuilder(domainName, adapterName);
        RequestDto[] requestsArray = requestsListDto.getRequests();
        for (RequestDto requestDto: requestsArray) {
            requestDto.buildDaoRequest(builder);
        }

        // Publish, i.e. save the requests.
        PublishTask publishTask = saasPublisher.createNewTask();
        saasPublisher.publishRequestsWithArray(publishTask, requestsArray);

        // TODO AsyncResponse ?  (to improve performance/parallelizm ?)
        //
        // Blocking wait for results.
        Exception[] results;
        try {
            results = publishTask.getResults();
        } catch (InterruptedException e) {
            // Something went wrong. We need to return '500' HTTP status code.
            throw new RuntimeException("Unexpected error. The request could not have been created. Please re-try later.");
        }

        // Build response.
        MultiResultResponse response = new MultiResultResponse();
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

        logger.info("createRequestsWithList - success");

        return response;
    }
}
