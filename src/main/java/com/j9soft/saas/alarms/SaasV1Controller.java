package com.j9soft.saas.alarms;

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
    SaasV1Controller(SaasV1Service saasService) {

        this.saasService = saasService;
    }

    @PostConstruct
    public void init() {
        Assert.notNull(saasService, "saasService is null!");
        logger.info("saasService is not null - OK");
    }

    @Override
    @PostMapping("/v1/domains/{domainName}/adapters/{adapterName}/request")
    public void createRequest(@PathVariable("domainName") String domainName,
                              @PathVariable("adapterName") String adapterName,
                              @RequestBody String requestDTOAsJson) {

        logger.info("createRequest(domainName='{}', adapterName='{}') - requestDTOAsJson:'{}'",
                domainName, adapterName, requestDTOAsJson);

        saasService.createRequest(domainName, adapterName, requestDTOAsJson);
    }


    @Override
    @PostMapping("/v1/domains/{domainName}/adapters/{adapterName}/request")
    public void createRequestsWithList(@PathVariable("domainName") String domainName,
                                       @PathVariable("adapterName") String adapterName,
                                       @RequestBody String requestDTOArrayAsJson) {

        logger.info("createRequestsWithList(domainName='{}', adapterName='{}')", domainName, adapterName);

        // @TODO implement multi request method.
    }

}