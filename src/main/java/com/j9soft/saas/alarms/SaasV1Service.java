package com.j9soft.saas.alarms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.j9soft.saas.alarms.model.CreateEntityRequest;
import com.j9soft.saas.alarms.model.Definitions;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SaasV1Service {
    public void createEntityRequest(SaasDao saasDao, String domainName, String adapterName,
                                    String entityTypeName, JsonNode requestDTORootNode) {

        JsonNode alarmDtoNode = requestDTORootNode.path("alarm_dto");
        if (alarmDtoNode.isMissingNode()) {
            throw new RuntimeException("@TODO: better exception handling");
        }

        JsonNode notificationIdentifierNode = alarmDtoNode.path("notification_identifier");
        if (notificationIdentifierNode.isMissingNode()) {
            throw new RuntimeException("@TODO: better exception handling");
        }

        JsonNode eventTimeNode = alarmDtoNode.path("event_time");
        if (eventTimeNode.isMissingNode()) {
            throw new RuntimeException("@TODO: better exception handling");
        }
        OffsetDateTime eventTimeInstant = OffsetDateTime.parse(eventTimeNode.textValue()); // https://stackoverflow.com/questions/6038136/how-do-i-parse-rfc-3339-datetimes-with-java#6038922

        // Question: Why do we use CreateEntityRequest and not CreateSourceAlarmRequest ?
        // Answer: Topic is for generic entities (Alarms, other objects) and generic subdomains (Adapters, other groups of objects:
        //   CreateEntityRequest(uuid,entityTypeName(string)=SourceAlarm,domainName,subdomainName=adapterName,lineageStartDate,entityIdInSubdomain=notificationIdentifier,attributesMap)
        //   ResyncAllStartSubdomainRequest(uuid,subdomainName=adapterName,entityTypeName(string)=SourceAlarm)
        //
        // Note: We do not generalize any further (e.g. to have EntityRequest with requestType,  SubdomainRequest with requestType)
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
        CreateEntityRequest request = CreateEntityRequest.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(entityTypeName)
                .setEntityDomainName(domainName)
                .setEntitySubdomainName(adapterName)
                .setEntityIdInSubdomain(notificationIdentifierNode.textValue())
                .setEventDate(eventTimeInstant.toInstant().toEpochMilli())  // @TODO add event_date to REST request body ??  because DomainRequests does not have event_time field.
                .build();

        // Forward to Dao.
        saasDao.createRequest(request);
    }
}
