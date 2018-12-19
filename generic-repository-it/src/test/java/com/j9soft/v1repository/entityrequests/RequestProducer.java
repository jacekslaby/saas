package com.j9soft.v1repository.entityrequests;

import com.j9soft.krepository.v1.commandsmodel.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * Implementation of data access layer (DAO) based on a Kafka producer.
 */
public class RequestProducer {

    private static final Logger logger = LoggerFactory.getLogger(RequestProducer.class);

    private String topicName;
    private KafkaProducer<String, Object> producer;

    /**
     * Note: Autowire - The idea is that it is possible to create a new class annotated as @Configuration
     *  and this class will get autowired here. (btw: In this class it is important to remember about a destroy method to close a producer.)
     */
    // @TODO @Autowired
    public RequestProducer() {
        this.topicName = KafkaConnector.getCommandsTopicName();
        this.producer = KafkaConnector.connectProducer();
    }

    public void sendNewRequest(CreateEntityRequestV1 request) throws ExecutionException, InterruptedException {

        logger.info("RequestUuid:{} - sendNewRequest(CreateEntityRequestV1)", request.getUuid());

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                this.topicName, request.getEntityIdInSubdomain().toString(), request);
        this.producer.send(record).get();
    }

    public void sendNewRequest(DeleteEntityRequestV1 request) throws ExecutionException, InterruptedException {
        logger.info("RequestUuid:{} - sendNewRequest(DeleteEntityRequestV1)", request.getUuid());

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                this.topicName, request.getEntityIdInSubdomain().toString(), request);
        this.producer.send(record).get();
    }

    public void sendNewRequest(ResyncAllStartSubdomainRequestV1 request) throws ExecutionException, InterruptedException {
        logger.info("RequestUuid:{} - sendNewRequest(ResyncAllStartSubdomainRequestV1)", request.getUuid());

        // @TODO send to every partition, with key "dummy"
        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record).get();
    }

    public void sendNewRequest(ResyncAllEndSubdomainRequestV1 request) throws ExecutionException, InterruptedException {
        logger.info("RequestUuid:{} - sendNewRequest(ResyncAllEndSubdomainRequestV1)", request.getUuid());

        // @TODO send to every partition, with key "dummy"
        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record).get();
    }

    public void sendNewRequest(UknownEntityRequestV1 request) throws ExecutionException, InterruptedException {

        logger.info("RequestUuid:{} - sendNewRequest(UknownEntityRequestV1)", request.getUuid());

        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record).get();
    }

    public void close() {
        if (this.producer != null) {
            this.producer.close();
        }
    }
}
