package com.j9soft.saas.alarms.dao;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllEndSubdomainRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllStartSubdomainRequestV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementation of data access layer (DAO) that does nothing, i.e. it is like a sink which accepts every created request.
 *
 * This Dao is used in dev mode, i.e. in development environments.
 * (BTW: In production mode a different Dao is used. One which connects to a real DB.)
 */
@Profile("default")
@Service
public class RequestDaoDevMock implements RequestDao {

    private static final Logger logger = LoggerFactory.getLogger(RequestDaoDevMock.class);

    @Override
    public void saveNewRequest(CreateEntityRequestV1 request, RequestDao.Callback callback) {
        logger.info("RequestUuid:{} - saveNewRequest(CreateEntityRequestV1)", request.getUuid());
        callback.onCompletion(null);
    }

    @Override
    public void saveNewRequest(DeleteEntityRequestV1 request, RequestDao.Callback callback) {
        logger.info("RequestUuid:{} - saveNewRequest(DeleteEntityRequestV1)", request.getUuid());
        callback.onCompletion(null);
    }

    @Override
    public void saveNewRequest(ResyncAllStartSubdomainRequestV1 request, RequestDao.Callback callback) {
        logger.info("RequestUuid:{} - saveNewRequest(ResyncAllStartSubdomainRequestV1)", request.getUuid());
        callback.onCompletion(null);
    }

    @Override
    public void saveNewRequest(ResyncAllEndSubdomainRequestV1 request, RequestDao.Callback callback) {
        logger.info("RequestUuid:{} - saveNewRequest(ResyncAllEndSubdomainRequestV1)", request.getUuid());
        callback.onCompletion(null);
    }

}
