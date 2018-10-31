package com.j9soft.saas.alarms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementation of publisher layer based on a Kafka DAO.
 *
 * This publisher Service is used in production mode, i.e. in production environments.
 */
@Profile({"prod","kafka-dev"})
@Service
public class SaasPublisherKafka implements SaasPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SaasPublisherKafka.class);

    private SaasDaoKafka saasDaoKafka;

    /**
     * Note: Autowire - The idea is that it is possible to create a new class annotated as @Configuration
     *  and this class will get autowired here. (btw: In this class it is important to remember about a destroy method to close a producer.)
     */
    @Autowired
    SaasPublisherKafka(SaasDaoKafka saasDaoKafka) {
        this.saasDaoKafka = saasDaoKafka;
    }

    @Override
    public void createRequest(SaasPublisher.Request request) {

        request.accept(this.saasDaoKafka);

        // @TODO introduce transactions (btw: and idempotency comes with transactions)
    }

    @Override
    public void createRequestsWithList(SaasPublisher.Request[] requests) {

        for (SaasPublisher.Request request: requests) {
            request.accept(this.saasDaoKafka);
        }

        // @TODO introduce transactions (btw: and idempotency comes with transactions)
    }
}
