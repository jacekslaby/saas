package com.j9soft.saas.alarms.testdata;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllEndSubdomainRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllStartSubdomainRequestV1;
import com.j9soft.saas.alarms.dao.DaoRequestBuilderV1;
import org.openapitools.model.CreateAlarmRequest;
import org.openapitools.model.DeleteAlarmRequest;

/**
 * For test purposes this builder additionally gathers created all DAO requests.
 * Later this objects are used to verify and assert method calls and parameters.
 */
public class TestDaoRequestsBuilder extends DaoRequestBuilderV1 {

    public static final String DOMAIN = "controllerTest";
    public static final String ADAPTER_NAME = "controllerTestAdapter";
    private static final String ALARM_NOID = "eric2g:341";

    private CreateEntityRequestV1 createEntityRequestV1;
    private DeleteEntityRequestV1 deleteEntityRequestV1;
    private ResyncAllEndSubdomainRequestV1 resyncAllEndSubdomainRequestV1;
    private ResyncAllStartSubdomainRequestV1 resyncAllStartSubdomainRequestV1;

    private String domainName;
    private String adapterName;

    private CreateAlarmRequest createAlarmRequest;

    public static TestDaoRequestsBuilder newBuilder() {
        return new TestDaoRequestsBuilder();
    }

    private TestDaoRequestsBuilder() {
        super(DOMAIN, ADAPTER_NAME);

    }

    public String getDomainName() {
        return DOMAIN;
    }

    public String getAdapterName() {
        return ADAPTER_NAME;
    }

    public CreateEntityRequestV1 getCreateEntityRequest() {
        return createEntityRequestV1;
    }

    public DeleteEntityRequestV1 getDeleteEntityRequest() {
        return deleteEntityRequestV1;
    }

    public ResyncAllEndSubdomainRequestV1 getResyncAllEndSubdomainRequest() {
        return resyncAllEndSubdomainRequestV1;
    }

    public ResyncAllStartSubdomainRequestV1 getResyncAllStartSubdomainRequest() {
        return resyncAllStartSubdomainRequestV1;
    }

    @Override
    public CreateEntityRequestV1 buildCreateEntityRequest(CreateAlarmRequest createAlarmRequest) {

        createEntityRequestV1 = super.buildCreateEntityRequest(createAlarmRequest);
        return createEntityRequestV1;
    }

    @Override
    public DeleteEntityRequestV1 buildDeleteEntityRequest(DeleteAlarmRequest deleteAlarmRequest) {

        deleteEntityRequestV1 = super.buildDeleteEntityRequest(deleteAlarmRequest);
        return deleteEntityRequestV1;
    }

    @Override
    public ResyncAllEndSubdomainRequestV1 buildResyncAllEnd() {

        resyncAllEndSubdomainRequestV1 = super.buildResyncAllEnd();
        return resyncAllEndSubdomainRequestV1;
    }

    @Override
    public ResyncAllStartSubdomainRequestV1 buildResyncAllStart() {

        resyncAllStartSubdomainRequestV1 = super.buildResyncAllStart();
        return resyncAllStartSubdomainRequestV1;
    }

}
