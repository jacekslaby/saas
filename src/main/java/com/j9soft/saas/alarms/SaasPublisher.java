package com.j9soft.saas.alarms;

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
    void createRequest(Request request);

    /**
     * Publish new requests. (e.g. of type DeleteEntityRequestV1(ed), CreateEntityRequestV1(ed)).
     * It is an atomic change. (i.e. all or nothing)
     */
    void createRequestsWithList(Request[] requests);

    interface Request {
        void accept(SaasDao visitor);

    }

    class CreateEntityRequest implements Request {

        private CreateEntityRequestV1 wrappedRequest;

        public static CreateEntityRequest newBuilder() {
            return new CreateEntityRequest();
        }

        public CreateEntityRequest setWrappedRequest(CreateEntityRequestV1 wrappedRequest) {
            this.wrappedRequest = wrappedRequest;
            return this;
        }

        @Override
        public void accept(SaasDao visitor) {
            visitor.createRequest(this.wrappedRequest);
        }
    }

    class DeleteEntityRequest implements Request {

        private DeleteEntityRequestV1 wrappedRequest;

        public static DeleteEntityRequest newBuilder() {
            return new DeleteEntityRequest();
        }

        public DeleteEntityRequest setWrappedRequest(DeleteEntityRequestV1 wrappedRequest) {
            this.wrappedRequest = wrappedRequest;
            return this;
        }

        @Override
        public void accept(SaasDao visitor) {
            visitor.createRequest(this.wrappedRequest);
        }
    }

    class ResyncAllStartSubdomainRequest implements Request {

        private ResyncAllStartSubdomainRequestV1 wrappedRequest;

        public static ResyncAllStartSubdomainRequest newBuilder() {
            return new ResyncAllStartSubdomainRequest();
        }

        public ResyncAllStartSubdomainRequest setWrappedRequest(ResyncAllStartSubdomainRequestV1 wrappedRequest) {
            this.wrappedRequest = wrappedRequest;
            return this;
        }

        @Override
        public void accept(SaasDao visitor) {
            visitor.createRequest(this.wrappedRequest);
        }
    }

    class ResyncAllEndSubdomainRequest implements Request {

        private ResyncAllEndSubdomainRequestV1 wrappedRequest;

        public static ResyncAllEndSubdomainRequest newBuilder() {
            return new ResyncAllEndSubdomainRequest();
        }

        public ResyncAllEndSubdomainRequest setWrappedRequest(ResyncAllEndSubdomainRequestV1 wrappedRequest) {
            this.wrappedRequest = wrappedRequest;
            return this;
        }

        @Override
        public void accept(SaasDao visitor) {
            visitor.createRequest(this.wrappedRequest);
        }
    }
}
