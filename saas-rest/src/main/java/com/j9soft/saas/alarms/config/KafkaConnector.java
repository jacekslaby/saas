package com.j9soft.saas.alarms.config;

import org.apache.kafka.clients.producer.KafkaProducer;


/**
 * Provider of producer connected to a Kafka cluster and to Kafka Schema Registry.
 */
public abstract class KafkaConnector {

    private static final String TOPIC_NAME__COMMANDS = "v1-commands-topic";

    public abstract KafkaProducer<String, Object> getProducer();

    public String getTopicName() {
        return TOPIC_NAME__COMMANDS;
    }

    public abstract void close();

}
