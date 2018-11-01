package com.j9soft.saas.alarms.service;

import com.j9soft.saas.alarms.dao.SaasDao;
import com.j9soft.saas.alarms.model.CreateEntityRequestV1;
import com.j9soft.saas.alarms.model.DeleteEntityRequestV1;
import com.j9soft.saas.alarms.model.ResyncAllEndSubdomainRequestV1;
import com.j9soft.saas.alarms.model.ResyncAllStartSubdomainRequestV1;

/**
 * Objects with this interface are used by Saas service (SaasV1Service)
 * as an intermediate layer to DAO. (SaasDao)
 */
public interface SaasPublisher {
    /**
     * Publish a new request. (e.g. of type DeleteEntityRequestV1(ed), CreateEntityRequestV1(ed)).
     * It is an atomic change. (i.e. all or nothing)
     */
    void publishRequest(Request request);

    /**
     * Publish new requests. (e.g. of type DeleteEntityRequestV1(ed), CreateEntityRequestV1(ed)).
     * It is an atomic change. (i.e. all or nothing)
     */
    void publishRequestsWithArray(Request[] requests);

    /**
     * Wrapper object which contains a request as defined in DAO layer. (e.g. an instance of CreateEntityRequestV1)
     *
     * Note: The implementing classes are Elements from Visitor pattern. (https://en.wikipedia.org/wiki/Visitor_pattern)
     *  (and Visitor classes implement SaasDao)
     */
    interface Request {
        void accept(SaasDao visitor);
    }

    class CreateEntityRequest implements Request {

        private CreateEntityRequestV1 wrappedDaoRequest;

        public static CreateEntityRequest newBuilder() {
            return new CreateEntityRequest();
        }

        public CreateEntityRequest setWrappedRequest(CreateEntityRequestV1 wrappedDaoRequest) {
            this.wrappedDaoRequest = wrappedDaoRequest;
            return this;
        }

        @Override
        public void accept(SaasDao visitor) {
            visitor.createRequest(this.wrappedDaoRequest);
        }
    }

    class DeleteEntityRequest implements Request {

        private DeleteEntityRequestV1 wrappedDaoRequest;

        public static DeleteEntityRequest newBuilder() {
            return new DeleteEntityRequest();
        }

        public DeleteEntityRequest setWrappedRequest(DeleteEntityRequestV1 wrappedDaoRequest) {
            this.wrappedDaoRequest = wrappedDaoRequest;
            return this;
        }

        @Override
        public void accept(SaasDao visitor) {
            visitor.createRequest(this.wrappedDaoRequest);
        }
    }

    class ResyncAllStartSubdomainRequest implements Request {

        private ResyncAllStartSubdomainRequestV1 wrappedDaoRequest;

        public static ResyncAllStartSubdomainRequest newBuilder() {
            return new ResyncAllStartSubdomainRequest();
        }

        public ResyncAllStartSubdomainRequest setWrappedRequest(ResyncAllStartSubdomainRequestV1 wrappedDaoRequest) {
            this.wrappedDaoRequest = wrappedDaoRequest;
            return this;
        }

        @Override
        public void accept(SaasDao visitor) {
            visitor.createRequest(this.wrappedDaoRequest);
        }
    }

    class ResyncAllEndSubdomainRequest implements Request {

        private ResyncAllEndSubdomainRequestV1 wrappedDaoRequest;

        public static ResyncAllEndSubdomainRequest newBuilder() {
            return new ResyncAllEndSubdomainRequest();
        }

        public ResyncAllEndSubdomainRequest setWrappedRequest(ResyncAllEndSubdomainRequestV1 wrappedDaoRequest) {
            this.wrappedDaoRequest = wrappedDaoRequest;
            return this;
        }

        @Override
        public void accept(SaasDao visitor) {
            visitor.createRequest(this.wrappedDaoRequest);
        }
    }
}
