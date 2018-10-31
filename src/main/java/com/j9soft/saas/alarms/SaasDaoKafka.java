package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.model.CreateEntityRequestV1;
import com.j9soft.saas.alarms.model.DeleteEntityRequestV1;
import com.j9soft.saas.alarms.model.ResyncAllEndSubdomainRequestV1;
import com.j9soft.saas.alarms.model.ResyncAllStartSubdomainRequestV1;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
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
    }

    @Override
    public void createRequest(DeleteEntityRequestV1 request) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record);
    }

    @Override
    public void createRequest(ResyncAllStartSubdomainRequestV1 request) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record);
    }

    @Override
    public void createRequest(ResyncAllEndSubdomainRequestV1 request) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record);
    }
}
