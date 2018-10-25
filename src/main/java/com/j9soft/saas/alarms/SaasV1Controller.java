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


    @Autowired
    SaasV1Controller(SaasDao saasDao) {

        this.saasDao = saasDao;
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

        logger.info("createRequest(domainName='{}', adapterName='{}')", domainName, adapterName);

        // @TODO validate requestDTOAsJson  (using OpenAPI specification)
        //

        // @TODO translate to schemas used in Saas (i.e. to schemas used in Kafka topic)  (or maybe to avro directly ? jackson)
        // so we need to create a Service in spring, which will do it for us  ( https://techsparx.com/software-development/openapi/spring-boot-rest-api-docs.html "Controller class(es)")
        // ? and perhaps "The methodology used here is to implement a fluid API to the Model class. " ?
        //
        //   AlarmEntityRequest(uuid,typeName/entityTypeName=SourceAlarm,domainName,subdomainName,idInSubdomain,requestType,lineageStartDate)
        //     ^^^^ Doubts: in Avro adding a new value to an enum is NOT forward compatible.  (I mean requestType=CreateAlarm/etc.)
        //             (i.e. old clients would not understand a value written by a new producer. Safer is to add a new schema, which may be ignored by old clients.)
        //
        //   AlarmSubdomainRequest(uuid,subdomainName,requestType=ResyncAllStart)
        //
        // If topic dedicated for Alarms and Adapters:
        //   CreateAlarmRequest(uuid,entityTypeName=SourceAlarm,domainName,adapterName,lineageStartDate,notificationIdentifier,attributesMap)
        //   AdapterResyncAllStartRequest(uuid,adapterName)       // btw: no domainName because concerns alarms from a single adapter
        //
        // If topic for Alarms and Adapters/OtherGroupsOfAlarms:
        //   CreateAlarmRequest(uuid,entityTypeName=SourceAlarm,domainName,adapterName,lineageStartDate,notificationIdentifier,attributesMap)
        //   SubdomainResyncAllStartRequest(uuid,subdomainName=adapterName,entityTypeName(string)=SourceAlarm)
        //
        // If topic for Alarms/OtherEntities and Adapters/OtherGroupsOfAlarms:
        //   CreateEntityRequest(uuid,entityTypeName(string)=SourceAlarm,domainName,subdomainName=adapterName,lineageStartDate,entityIdInSubdomain=notificationIdentifier,attributesMap)
        //   ResyncAllStartSubdomainRequest(uuid,subdomainName=adapterName,entityTypeName(string)=SourceAlarm)
        //
        // BUT we do not generalize any further (e.g. to have EntityRequest with requestType,  SubdomainRequest with requestType)
        //  because we will introduce new request types later (i.e. new schemas within the same version v1)
        //  and we want these changes to be forward compatible.
        //   (i.e. If we added a new value to an enum  (e.g. 'DeleteRequest' to EntityRequest.requestType)
        //     then old clients would not understand a request written by a new producer.
        //     Safer is to add a new request schema, which may be ignored by old clients.)
        //
        // Note: Topic schemas like CreateEntityRequest do not enforce Alarm attributes and their types (e.g. event_time type: string format: dateTime)
        //  because we do not want to update these schemas when we add Alarm attributes.
        //  (BUT where it is needed these types and their format may be enforced by Alarm REST service.
        //   (The REST service is easier to evolve and is dedicated to Source Alarms.))
        //

        // @TODO forward to Dao.
    }

    @Override
    @PostMapping("/v1/domains/{domainName}/adapters/{adapterName}/request")
    public void createRequestsWithList(@PathVariable("domainName") String domainName,
                                       @PathVariable("adapterName") String adapterName,
                                       @RequestBody String requestDTOArrayAsJson) {

        logger.info("createRequestsWithList(domainName='{}', adapterName='{}')", domainName, adapterName);
    }

}