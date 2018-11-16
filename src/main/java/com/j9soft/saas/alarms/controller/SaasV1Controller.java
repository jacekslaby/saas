package com.j9soft.saas.alarms.controller;

import com.j9soft.saas.alarms.SaasV1;
import com.j9soft.saas.alarms.model.RequestDto;
import com.j9soft.saas.alarms.model.RequestsListDto;
import com.j9soft.saas.alarms.service.SaasV1Service;
import org.openapitools.client.model.MultiStatusResponse;
import org.openapitools.client.model.RequestCreatedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * Servlet based implementation of Command API (v.1) of Source Active Alarms Store.
 *
 * This implementation is based on Spring Boot annotations for request mappings.
 * See https://spring.io/guides/gs/spring-boot/
 */
@RestController
public class SaasV1Controller implements SaasV1 {

    private static final Logger logger = LoggerFactory.getLogger(SaasV1Controller.class);

    private SaasV1Service saasService;

    @Autowired
    public SaasV1Controller(SaasV1Service saasService) {

        this.saasService = saasService;
    }

    @PostConstruct
    public void init() {
        Assert.notNull(saasService, "saasService is null!");
        logger.info("saasService is not null - OK");
    }

    @Override
    @PostMapping("/v1/domains/{domainName}/adapters/{adapterName}/request")
    public RequestCreatedResponse createRequest(@PathVariable("domainName") String domainName,
                                                @PathVariable("adapterName") String adapterName,
                                                @RequestBody RequestDto requestDto) {

        logger.info("createRequest(domainName='{}', adapterName='{}') - requestDto:'{}'",
                domainName, adapterName, requestDto);

        return saasService.createRequest(domainName, adapterName, requestDto);

        // https://www.baeldung.com/spring-request-response-body
        //  "Remember, we don’t need to annotate the @RestController-annotated controllers with the @ResponseBody annotation
        //   since it’s done by default here."
    }


    @Override
    @PostMapping("/v1/domains/{domainName}/adapters/{adapterName}/request")
    public MultiStatusResponse createRequestsWithList(@PathVariable("domainName") String domainName,
                                                      @PathVariable("adapterName") String adapterName,
                                                      @RequestBody RequestsListDto requestsListDto) {

        logger.info("createRequestsWithList(domainName='{}', adapterName='{}') - requestsListDto:'{}'",
                domainName, adapterName, requestsListDto);

        return saasService.createRequestsWithList(domainName, adapterName, requestsListDto);

        // @TODO Add logic to return 207 in case of a partial success.
        // (see https://stackoverflow.com/questions/16232833/how-to-respond-with-http-400-error-in-a-spring-mvc-responsebody-method-returnin#16250729
        //   The only way it can be done is by registering custom exception handler.
        //   (I mean the only way without introducing REST specific objects (ResponseEntity,etc.) into a generic SaasV1 interface.)
    }

}