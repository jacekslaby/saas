package com.j9soft.saas.alarms.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Properties;

/**
 * Configuration settings are read from system properties named "kafka-host" and "kafka-port".
 */
@Profile({"prod","kafka-dev"})
@Component
public class KafkaEnvConnector extends KafkaConnector {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEnvConnector.class);

    private KafkaProducer<String, Object> producer;


    @Autowired
    KafkaEnvConnector(@Value("${kafka-host}") String brokerHost,
                      @Value("${kafka-port:9092}") Integer brokerPort,
                      @Value("${kafka-schema-registry-url}") String schemaRegistryUrl) {

        if (logger.isInfoEnabled()) {
            logger.info("Connecting to Kafka with system properties: {}", Arrays.asList(
                    System.getProperties().entrySet().stream()
                            .filter(e -> ((String) e.getKey()).startsWith("kafka"))
                            .toArray())
            );
            logger.info("Connecting to Kafka at: {}:{}, Schema Registry at: {}", brokerHost, brokerPort, schemaRegistryUrl);
        }

        connect(brokerHost, brokerPort, schemaRegistryUrl);
    }

    private void connect(final String brokerHost, final Integer brokerPort, final String schemaRegistryUrl) {

        // Producer:
        //
        // Producer configuration.
        Properties producerProps = new Properties();
        producerProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerHost + ":" + brokerPort);
        producerProps.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "SaasProducer");  // TODO configuration ?  And maybe share it with tests connector.
        producerProps.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        producerProps.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        //
        // We want to have schemas under the fully-qualified record name. Across all topics.
        // (see also  https://docs.confluent.io/current/schema-registry/docs/serializer-formatter.html
        //   http://martin.kleppmann.com/2018/01/18/event-types-in-kafka-topic.html)
        producerProps.setProperty(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, "io.confluent.kafka.serializers.subject.RecordNameStrategy");
        producerProps.setProperty(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        producerProps.setProperty(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "false"); // TODO auto.register.schemas to true on Production ?? Check kafka recommendations.
        //
        // We need an idempotent producer because ordering of messages is strictly required.
        // (e.g. because if a ResyncAllStart is redelivered again after several CreateEntity requests
        //  then we would have lost all those entities during resynchronization)
        //
        // https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/
        // - "to get exactly-once semantics per partition - meaning no duplicates, no data loss, and in-order semantics -
        //    configure your producer to set “enable.idempotence=true”
        //
        producerProps.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        //
        // Create a new instance.
        producer = new KafkaProducer<>(producerProps);
        logger.info("Kafka producer: " + producer);
    }

    @Override
    public KafkaProducer<String, Object> getProducer() {
        return producer;
    }

    @Override
    public void close() {
        producer.close();
    }
}
