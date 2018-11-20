package com.j9soft.saas.alarms.dao;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllEndSubdomainRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllStartSubdomainRequestV1;

/**
 * Interface defining what functionality is provided by a data access layer.
 *
 * It is also a Visitor (https://en.wikipedia.org/wiki/Visitor_pattern)
 *  to objects of class RequestDto.
 * During a visit (i.e. when a RequestDto accepts a RequestDao in RequestDto.saveInDao(RequestDao))
 *  an appropriate method of RequestDao is invoked. (e.g. #saveNewRequest(CreateEntityRequestV1 request)
 */
public interface RequestDao {
    /**
     * Create a new request of type CreateEntityRequest(ed).
     */
    void saveNewRequest(CreateEntityRequestV1 request, Callback callback);

    /**
     * Create a new request of type DeleteEntityRequest(ed).
     */
    void saveNewRequest(DeleteEntityRequestV1 request, Callback callback);

    /**
     * Create a new request of type ResyncAllStartSubdomainRequest(ed).
     */
    void saveNewRequest(ResyncAllStartSubdomainRequestV1 request, Callback callback);

    /**
     * Create a new request of type ResyncAllEndSubdomainRequest(ed).
     */
    void saveNewRequest(ResyncAllEndSubdomainRequestV1 request, Callback callback);

    /**
     * Callback method onCompletion is invoked after request is processed.
     * (possibly asynchronously, i.e. from another thread)
     */
    public interface Callback {
        /**
         * @param exception If any exception happened then it is provided. Otherwise null is delivered.
         */
        public void onCompletion(Exception exception);
    }

}
