package com.j9soft.saas.alarms.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.j9soft.saas.alarms.dao.DaoRequestBuilder;
import com.j9soft.saas.alarms.dao.RequestDao;

/**
 * A request which should be persisted by SaasV1 microservice.
 *
 * The requests arrive to REST endpoint as JSON objects matching schemas defined in OpenAPI specification.
 * Spring Boot translates JSON objects to Java objects (thanks to @RequestBody annotation).
 * Although there are Java classes auto-generated from the OpenAPI specification
 * it is not possible to directly use them as parameters to REST controller methods.
 * The reason is that we need to help Jackson to properly read JSON objects by adding additional annotations
 * and that is why we need class RequestDto and its subclasses.
 *
 * Note: Name RequestDto is used to easier differentiate this class from the class Request which is auto-generated from OpenAPI spec.
 * (The same applies to its subclasses, e.g. CreateAlarmRequestDto is the equivalent of CreateAlarmRequest from OpenAPI spec.)
 *
 * Both abstract methods of this class use the Visitor pattern,
 * i.e. the implementing classes are expected to provide logic which presents themselves to a visitor.
 *  (e.g. to a RequestDao visitor when a request is to be saved)
 *
 * The lifecycle of a RequestDto is as follows:
 * - created (e.g. deserialized from JSON provided in body of a REST request)
 * - visited by DaoRequestBuilder object to prepare request representation (i.e. a DAO request)
 *     comprehensible for Data Access layer. (i.e. comprehensible for RequestDao object)
 *     E.g. to create an instance of CreateEntityRequestV1 which is representation understood by DAO.
 * - visited by RequestDao object to save request (i.e. the DAO request) in Data Access layer.
 *
 * Note: This class is used as a convenient wrapper
 *  allowing us to use Jackson deserialization  (and its support for polymorphic types)
 *  when deserializing OpenAPI requests received by the SaasV1Controller.
 *  (Similarly its subclasses exist for the same purpose - to be able to leverage Jackson deserialization.)
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "request_type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateAlarmRequestDto.class, name = "CreateAlarmRequest"),
    @JsonSubTypes.Type(value = DeleteAlarmRequestDto.class, name = "DeleteAlarmRequest"),
    @JsonSubTypes.Type(value = ResyncAllAlarmsEndRequestDto.class, name = "ResyncAllAlarmsEndRequest"),
    @JsonSubTypes.Type(value = ResyncAllAlarmsStartRequestDto.class, name = "ResyncAllAlarmsStartRequest")})
public abstract class RequestDto {

    /**
     * Accept DaoRequestBuilder visitor and delegate itself to the visitor in order to build own DAO request.
     */
    public abstract void buildDaoRequest(DaoRequestBuilder daoRequestBuilder);

    /**
     * Accept DAO visitor and delegate own DAO request to the visitor in order to have it saved.
     */
    public abstract void saveInDao(RequestDao requestDao, RequestDao.Callback callback);

}
