package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.model.CreateEntityRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementation of data access layer (DAO) based on a hashmap kept in memory.
 *
 * This Dao is used in dev mode, i.e. in development environments.
 * (BTW: In prod (i.e. production) mode a different Dao is used. One which connects to a real DB.)
 */
@Profile("default")
@Service
public class SaasDaoDevMock implements SaasDao {

    @Override
    public void createRequest(CreateEntityRequest request) {

    }
}
