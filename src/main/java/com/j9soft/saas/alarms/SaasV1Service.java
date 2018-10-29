package com.j9soft.saas.alarms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j9soft.saas.alarms.model.CreateEntityRequest;
import com.j9soft.saas.alarms.model.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.j9soft.saas.alarms.model.Definitions.*;

@Service
public class SaasV1Service {

    // @Autowired  - is not used because:
    // https://spring.io/blog/2016/04/15/testing-improvements-in-spring-boot-1-4
    // "Don’t use field injection as it just makes your tests harder to write."
    //
    private final SaasDao saasDao;

    @Autowired
    SaasV1Service(SaasDao saasDao) {

        this.saasDao = saasDao;
    }

    void createRequest(String domainName, String adapterName, String requestDTOAsJson) {
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
                createEntityRequest(this.saasDao, domainName, adapterName,
                        Definitions.ALARM_ENTITY_TYPE_NAME, rootNode);
                break;
            default:
                throw new RuntimeException("@TODO better exception handling and returning results: " + requestType);
        }
    }

    private void createEntityRequest(SaasDao saasDao, String domainName, String adapterName,
                                    String entityTypeName, JsonNode requestDTORootNode) {

        JsonNode alarmDtoNode = requestDTORootNode.path(API_SCHEMA_ALARM__ALARM_DTO);
        if (alarmDtoNode.isMissingNode()) {
            throw new RuntimeException("@TODO: better exception handling");
        }

        JsonNode notificationIdentifierNode = alarmDtoNode.path(API_SCHEMA_ALARM__NOTIFICATION_IDENTIFIER);
        if (notificationIdentifierNode.isMissingNode() || notificationIdentifierNode.textValue().length() < 1) {
            throw new RuntimeException("@TODO: better exception handling");
        }

        JsonNode eventTimeNode = alarmDtoNode.path(API_SCHEMA_ALARM__EVENT_TIME);
        if (eventTimeNode.isMissingNode() || eventTimeNode.textValue().length() < 1) {
            throw new RuntimeException("@TODO: better exception handling");
        }
        OffsetDateTime eventTimeInstant = OffsetDateTime.parse(eventTimeNode.textValue()); // https://stackoverflow.com/questions/6038136/how-do-i-parse-rfc-3339-datetimes-with-java#6038922

        JsonNode perceivedSeverityNode = alarmDtoNode.path(API_SCHEMA_ALARM__PERCEIVED_SEVERITY);
        if (perceivedSeverityNode.isMissingNode()
                || perceivedSeverityNode.intValue() < 0) {
            throw new RuntimeException("@TODO: better exception handling");
        }

        // Prepare map with Alarm attributes.
        //
        // - Read additional Alarm attributes to a HashMap.
        JsonNode alarmDtoAdditionalPropertiesNode = alarmDtoNode.path(API_SCHEMA_ALARM__ADDITIONAL_PROPERTIES);
        if (alarmDtoAdditionalPropertiesNode.isMissingNode()) {
            throw new RuntimeException("@TODO: better exception handling");
        }
        ObjectMapper mapper = new ObjectMapper();  // @TODO for performance reasons we should share ObjectMapper instance
        Map<CharSequence, CharSequence> alarmAttributes;
        alarmAttributes = mapper.convertValue(alarmDtoAdditionalPropertiesNode, HashMap.class);  // https://stackoverflow.com/questions/39237835/jackson-jsonnode-to-typed-collection
        //
        // - Add required attributes to the same HashMap.
        alarmAttributes.put(ALARM_ATTRIBUTE_NAME__EVENT_TIME, eventTimeNode.textValue());
        alarmAttributes.put(ALARM_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER, notificationIdentifierNode.textValue());
        alarmAttributes.put(ALARM_ATTRIBUTE_NAME__PERCEIVED_SEVERITY, String.valueOf(perceivedSeverityNode.intValue()));

        // Question: Why do we use a generic CreateEntityRequest type instead of a specialized CreateSourceAlarmRequest ?
        //
        // Answer: The target Topic is for _generic_ entities (e.g. Alarms, other objects)
        //  and generic subdomains (e.g. Adapters, other groups of objects). That is why we have:
        //   CreateEntityRequest(uuid,entityTypeName(string)=SourceAlarm,domainName,subdomainName=adapterName,lineageStartDate,entityIdInSubdomain=notificationIdentifier,attributesMap)
        //   ResyncAllStartSubdomainRequest(uuid,subdomainName=adapterName,entityTypeName(string)=SourceAlarm)
        //
        // Note: We do not generalize any further (e.g. to have EntityRequest with requestType=CreateEntity,
        //    SubdomainRequest with requestType=ResyncAllStart)
        //  because we will introduce new request types later (i.e. new schemas within the same version v1)
        //  and we want these changes to be forward compatible.
        //   (i.e. Had we added a new value to an enum  (e.g. 'DeleteRequest' to EntityRequest.requestType)
        //     then old clients would not have understood a request written by a new producer.
        //     Safer is to add a new request schema (i.e. a new class), which may be ignored by old clients.)
        //
        // Note: Topic schemas like CreateEntityRequest do not enforce Alarm attributes
        //  nor their types (e.g. that event_time has type: string format: dateTime)
        //  because we do not want to update these Kafka schemas when we add Alarm attributes.
        //  (BUT where it is needed these types and their format may be enforced by Alarm REST service.
        //   The REST service is easier to evolve and is dedicated to Source Alarms.)
        //
        CreateEntityRequest request = CreateEntityRequest.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(entityTypeName)
                .setEntityDomainName(domainName)
                .setEntitySubdomainName(adapterName)
                .setEntityIdInSubdomain(notificationIdentifierNode.textValue())
                .setEventDate(eventTimeInstant.toInstant().toEpochMilli())  // @TODO add event_date to REST request body ??  because DomainRequests does not have event_time field.
                .setEntityAttributes(alarmAttributes)
                .build();

        // Forward to Dao.
        saasDao.createRequest(request);
    }
}
