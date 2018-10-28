package com.j9soft.saas.alarms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j9soft.saas.alarms.model.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Servlet based implementation of Source Active Alarms Store API (v.1).
 *
 * This implementation is based on Spring Boot annotations for request mappings.
 * See https://spring.io/guides/gs/spring-boot/
 */
@RestController
public class SaasV1Controller implements SaasV1 {

    private static final Logger logger = LoggerFactory.getLogger(SaasV1Controller.class);

    // @Autowired  - is not used because:
    // https://spring.io/blog/2016/04/15/testing-improvements-in-spring-boot-1-4
    // "Don’t use field injection as it just makes your tests harder to write."
    //
    private final SaasDao saasDao;

    private SaasV1Service saasService;

    @Autowired
    SaasV1Controller(SaasDao saasDao, SaasV1Service saasService) {

        this.saasDao = saasDao;
        this.saasService = saasService;
    }

    @PostConstruct
    public void init() {
        Assert.notNull(saasDao, "saasDao is null!");
        logger.info("saasDao is not null - OK");
    }

    @Override
    @PostMapping("/v1/domains/{domainName}/adapters/{adapterName}/request")
    public void createRequest(@PathVariable("domainName") String domainName,
                              @PathVariable("adapterName") String adapterName,
                              @RequestBody String requestDTOAsJson) {

        logger.info("createRequest(domainName='{}', adapterName='{}') - requestDTOAsJson:'{}'",
                domainName, adapterName, requestDTOAsJson);

        // @TODO validate requestDTOAsJson  (using OpenAPI specification)
        //
        ObjectMapper mapper = new ObjectMapper();  // @TODO for performance reasons we should share ObjectMapper instance
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(requestDTOAsJson);
        } catch (IOException e) {
            throw new RuntimeException("@TODO better exception handling and returning results", e);
        }
        JsonNode requestTypeNode = rootNode.path("request_type");
        if (requestTypeNode.isMissingNode()) {
            throw new RuntimeException("@TODO: better exception handling: rootNode JSON: " + rootNode.asText());
        }
        String requestType = requestTypeNode.asText();


        // Translate to schemas used in Saas (i.e. to schemas used in Kafka topic)
        //  and forward the result request object to Dao.
        //
        switch (requestType) {
            case "CreateAlarm":
                saasService.createEntityRequest(this.saasDao, domainName, adapterName,
                        Definitions.ALARM_ENTITY_TYPE_NAME, rootNode);
                break;
            default:
                throw new RuntimeException("@TODO better exception handling and returning results: " + requestType);
        }
    }

    @Override
    @PostMapping("/v1/domains/{domainName}/adapters/{adapterName}/request")
    public void createRequestsWithList(@PathVariable("domainName") String domainName,
                                       @PathVariable("adapterName") String adapterName,
                                       @RequestBody String requestDTOArrayAsJson) {

        logger.info("createRequestsWithList(domainName='{}', adapterName='{}')", domainName, adapterName);
    }

}