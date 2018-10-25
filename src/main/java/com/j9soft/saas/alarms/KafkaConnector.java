package com.j9soft.saas.alarms;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 * Provider of producer and consumer connected to a Kafka cluster.
 */
public class KafkaConnector {

    private static final String TOPIC_NAME_RAW_ACTIVE_ALARMS = "tc_raw_active_alarms";

    private static final Logger logger = LoggerFactory.getLogger(KafkaConnector.class);

    private KafkaProducer<String, byte[]> producer;

    void connect(final String brokerHost, final Integer brokerPort) {

        // Producer:
        //
        // Producer configuration.
        Properties producerProps = new Properties();
        producerProps.setProperty("bootstrap.servers", brokerHost + ":" + brokerPort);
        producerProps.setProperty("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        producerProps.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        //
        // Create a new instance.
        producer = new KafkaProducer<>(producerProps);
        logger.info("Kafka producer: " + producer);
    }

    public KafkaProducer<String, byte[]> getProducer() {
        return this.producer;
    }

    public String getTopicName() {
        return TOPIC_NAME_RAW_ACTIVE_ALARMS;
    }

    void close() {
        producer.close();
    }

}
