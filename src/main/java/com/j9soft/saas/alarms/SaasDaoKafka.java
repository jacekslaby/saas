package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.model.CreateEntityRequestV1;
import com.j9soft.saas.alarms.model.DeleteEntityRequestV1;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementation of data access layer (DAO) based on a Kafka producer.
 *
 * This Dao is used in production mode, i.e. in production environments.
 */
@Profile({"prod","kafka-dev"})
@Service
public class SaasDaoKafka implements SaasDao {

    private static final Logger logger = LoggerFactory.getLogger(SaasDaoKafka.class);

    private String topicName;
    private KafkaProducer<String, Object> producer;

    /**
     * Note: Autowire - The idea is that it is possible to create a new class annotated as @Configuration
     *  and this class will get autowired here. (btw: In this class it is important to remember about a destroy method to close a producer.)
     */
    @Autowired
    SaasDaoKafka(KafkaConnector connector) {
        this.topicName = connector.getTopicName();
        this.producer = connector.getProducer();
    }

    @Override
    public void createRequest(CreateEntityRequestV1 request) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record);

        // @TODO introduce transactions (btw: and idempotency comes with transactions)
    }

    @Override
    public void createRequest(DeleteEntityRequestV1 request) {

        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record);

        // @TODO introduce transactions (btw: and idempotency comes with transactions)
    }
}
