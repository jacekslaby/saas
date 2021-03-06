package com.j9soft.saas.alarms.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * A list of RequestDTOs which should be persisted by SaasV1 microservice.
 *
 * The requests arrive to REST endpoint as JSON objects matching schemas defined in OpenAPI specification.
 * Spring Boot translates JSON objects to Java objects (thanks to @RequestBody annotation).
 * Although there is Java class RequestsList auto-generated from the OpenAPI specification
 * it is not used in SaasV1 interface. Class RequestsListDto is used instead.
 */
public class RequestsListDto {

    // Note: Name of field on purpose is not according to Java coding conventions.
    //   Reason is that this name appears in REST response and we want it to match the name defined in OpenAPI spec.
    @NotNull
    @Valid   // See also: https://lmonkiewicz.com/programming/get-noticed-2017/spring-boot-rest-request-validation/
    @JsonProperty("requests_array")
    private RequestDto[] requests_array;

    public RequestDto[] getRequests() {
        return requests_array;
    }

    public void setRequests(RequestDto[] requests) {
        this.requests_array = requests;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RequestsListDto {\n");

        sb.append("    requests: ").append(toIndentedString(requests_array)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the requests to string.
     */
    private String toIndentedString(RequestDto[] requests) {
        if (requests == null) {
            return "null";
        } else if (requests.length < 1) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < requests.length - 1; i++) {
            sb.append(toIndentedString(requests[i])).append(",\n");
        }
        sb.append(toIndentedString(requests[requests.length - 1])).append("\n");  // last one is without a comma character
        sb.append("]");
        return sb.toString().replace("\n", "\n    ");
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "    null";
        }
        return "    " + o.toString().replace("\n", "\n    ");
    }
}
