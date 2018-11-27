package com.j9soft.saas.alarms.dao;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllEndSubdomainRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllStartSubdomainRequestV1;
import com.j9soft.saas.alarms.model.Definitions;
import org.openapitools.client.model.CreateAlarmRequest;
import org.openapitools.client.model.DeleteAlarmRequest;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.j9soft.saas.alarms.model.Definitions.*;

public class DaoRequestBuilderV1 implements DaoRequestBuilder {

    private String domainName;
    private String adapterName;
    private String entitySubdomainName;

    public static DaoRequestBuilderV1 newBuilder(String domainName, String adapterName) {
        return new DaoRequestBuilderV1(domainName, adapterName);
    }

    protected DaoRequestBuilderV1(String domainName, String adapterName) {
        this.domainName = domainName;
        this.adapterName = adapterName;
        entitySubdomainName = domainName + "/" + adapterName;
    }

    @Override
    public CreateEntityRequestV1 buildCreateEntityRequest(CreateAlarmRequest createAlarmRequest) {

        // https://stackoverflow.com/questions/6038136/how-do-i-parse-rfc-3339-datetimes-with-java#6038922
        OffsetDateTime eventTimeInstant = OffsetDateTime.parse(createAlarmRequest.getAlarmDto().getEventTime());

        if (createAlarmRequest.getAlarmDto().getPerceivedSeverity() < 0) {
            throw new RuntimeException("@TODO: better exception handling");
        }

        // Prepare map with Alarm attributes.
        //
        Map<CharSequence, CharSequence> alarmAttributes = new HashMap<>();
        // - Add additional Alarm attributes to a HashMap.
        alarmAttributes.putAll(createAlarmRequest.getAlarmDto().getAdditionalProperties());
        // - Add required attributes to the same HashMap.
        alarmAttributes.put(ALARM_ATTRIBUTE_NAME__EVENT_TIME, createAlarmRequest.getAlarmDto().getEventTime());
        alarmAttributes.put(ALARM_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER,
                createAlarmRequest.getAlarmDto().getNotificationIdentifier());
        alarmAttributes.put(ALARM_ATTRIBUTE_NAME__PERCEIVED_SEVERITY,
                String.valueOf(createAlarmRequest.getAlarmDto().getPerceivedSeverity()));

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
        return CreateEntityRequestV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(Definitions.ALARM_ENTITY_TYPE_NAME)
                .setEntitySubdomainName(entitySubdomainName)
                .setEntityIdInSubdomain(createAlarmRequest.getAlarmDto().getNotificationIdentifier())
                .setEventDate(eventTimeInstant.toInstant().toEpochMilli())  // @TODO add event_date to REST request body ??  because DomainRequests does not have event_time field.
                .setEntityAttributes(alarmAttributes)
                .build();
    }

    @Override
    public DeleteEntityRequestV1 buildDeleteEntityRequest(DeleteAlarmRequest deleteAlarmRequest) {

        if (deleteAlarmRequest.getAlarmDto().getNotificationIdentifier() == null
                || deleteAlarmRequest.getAlarmDto().getNotificationIdentifier().length() < 1) {
            throw new RuntimeException("@TODO: better exception handling");
        }

        // https://stackoverflow.com/questions/6038136/how-do-i-parse-rfc-3339-datetimes-with-java#6038922
        OffsetDateTime eventTimeInstant = OffsetDateTime.parse(deleteAlarmRequest.getAlarmDto().getEventTime());

        return DeleteEntityRequestV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(Definitions.ALARM_ENTITY_TYPE_NAME)
                .setEntitySubdomainName(entitySubdomainName)
                .setEntityIdInSubdomain(deleteAlarmRequest.getAlarmDto().getNotificationIdentifier())
                .setEventDate(eventTimeInstant.toInstant().toEpochMilli())  // @TODO add event_date to REST request body ??  because DomainRequests does not have event_time field.
                .build();
    }

    @Override
    public ResyncAllEndSubdomainRequestV1 buildResyncAllEnd() {

        long entryDate = System.currentTimeMillis();

        return ResyncAllEndSubdomainRequestV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(entryDate)
                .setEntityTypeName(Definitions.ALARM_ENTITY_TYPE_NAME)
                .setEntitySubdomainName(entitySubdomainName)
                .setEventDate(entryDate)  // @TODO add event_date to REST request body ??  because DomainRequests does not have event_time field.
                .build();
    }

    @Override
    public ResyncAllStartSubdomainRequestV1 buildResyncAllStart() {

        long entryDate = System.currentTimeMillis();

        return ResyncAllStartSubdomainRequestV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(entryDate)
                .setEntityTypeName(Definitions.ALARM_ENTITY_TYPE_NAME)
                .setEntitySubdomainName(entitySubdomainName)
                .setEventDate(entryDate)  // @TODO add event_date to REST request body ??  because DomainRequests does not have event_time field.
                .build();
    }

}
