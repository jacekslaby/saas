package com.j9soft.v1repository.entityrequests;

import com.j9soft.krepository.v1.commandsmodel.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
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
        topicName = KafkaConnector.getCommandsTopicName();
        producer = KafkaConnector.connectProducer();
    }

    public void sendNewRequest(CreateEntityRequestV1 request) throws ExecutionException, InterruptedException {

        logger.info("RequestUuid:{} - sendNewRequest(CreateEntityRequestV1)", request.getUuid());

        // Note: Messages regarding the same entity MUST always be sent to the same partition.
        //   The easiest way to achieve it is to use string value of entityIdInSubdomain as a Kafka message key,
        //     as it is done in the code below.
        //   (It works because default partitioner always assigns the same key value to the same partition number,
        //     and, on top of that, it does a good job of _evenly_ spreading the load between partitions.)
        //   (Note: It is fine if another subdomain also sends a kafka message with the same key value,
        //     because k-repository does not use message keys for its logic. It uses entityIdInSubdomain and entitySubdomainName.)
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                topicName, request.getEntityIdInSubdomain().toString(), request);
        producer.send(record).get();
    }

    public void sendNewRequest(DeleteEntityRequestV1 request) throws ExecutionException, InterruptedException {
        logger.info("RequestUuid:{} - sendNewRequest(DeleteEntityRequestV1)", request.getUuid());

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                topicName, request.getEntityIdInSubdomain().toString(), request);
        producer.send(record).get();
    }

    public void sendNewRequest(ResyncAllStartSubdomainRequestV1 request) throws ExecutionException, InterruptedException {
        logger.info("RequestUuid:{} - sendNewRequest(ResyncAllStartSubdomainRequestV1)", request.getUuid());

        // Subdomain request must be send to every partition of commands topic.
        for (PartitionInfo partitionInfo: producer.partitionsFor(topicName)) {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topicName, partitionInfo.partition(), 
                    request.getUuid().toString(), request);
            producer.send(record).get();
        }
    }

    public void sendNewRequest(ResyncAllEndSubdomainRequestV1 request) throws ExecutionException, InterruptedException {
        logger.info("RequestUuid:{} - sendNewRequest(ResyncAllEndSubdomainRequestV1)", request.getUuid());

        // Subdomain request must be send to every partition of commands topic.
        for (PartitionInfo partitionInfo: producer.partitionsFor(topicName)) {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topicName, partitionInfo.partition(),
                    request.getUuid().toString(), request);
            producer.send(record).get();
        }
    }

    public void sendNewRequest(UknownEntityRequestV1 request) throws ExecutionException, InterruptedException {

        logger.info("RequestUuid:{} - sendNewRequest(UknownEntityRequestV1)", request.getUuid());

        ProducerRecord<String, Object> record = new ProducerRecord<>(topicName, request.getUuid().toString(), request);
        producer.send(record).get();
    }

    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}
