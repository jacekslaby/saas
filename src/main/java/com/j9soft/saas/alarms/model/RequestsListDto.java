package com.j9soft.saas.alarms.model;

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

    @NotNull
    @Valid
    private RequestDto[] requests;

    public RequestDto[] getRequests() {
        return requests;
    }

    public void setRequests(RequestDto[] requests) {
        this.requests = requests;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RequestsListDto {\n");

        sb.append("    requests: ").append(toIndentedString(requests)).append("\n");
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
        sb.append(toIndentedString(requests[requests.length])).append("\n");
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
