package com.j9soft.saas.alarms.model;

/**
 * A list of RequestDTOs which should be persisted by SaasV1 microservice.
 *
 * The requests arrive to REST endpoint as JSON objects matching schemas defined in OpenAPI specification.
 * Spring Boot translates JSON objects to Java objects (thanks to @RequestBody annotation).
 * Although there is Java class RequestsList auto-generated from the OpenAPI specification
 * it is not used in SaasV1 interface. Class RequestsListDto is used instead.
 */
public class RequestsListDto {

    private RequestDto[] requests;

    public RequestDto[] getRequests() {
        return requests;
    }

    public void setRequests(RequestDto[] requests) {
        this.requests = requests;
    }
}
