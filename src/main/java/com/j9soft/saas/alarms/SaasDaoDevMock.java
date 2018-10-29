package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.model.CreateEntityRequestV1;
import com.j9soft.saas.alarms.model.DeleteEntityRequestV1;
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
public class SaasDaoDevMock implements SaasDao {

    @Override
    public void createRequest(CreateEntityRequestV1 request) {

    }

    @Override
    public void createRequest(DeleteEntityRequestV1 request) {

    }
}
