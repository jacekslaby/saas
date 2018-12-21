package com.j9soft.saas.alarms.testconfig;


import com.j9soft.saas.alarms.config.KafkaConnector;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Provider of producer connected to the embedded Kafka cluster, embedded ZooKeeper, and to mock Kafka Schema Registry.
 */
public class KafkaTestConnector extends KafkaConnector {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTestConnector.class);

    private KafkaProducer<String, Object> producer;

    void connect(final String brokerHost, final Integer brokerPort) {

        // Producer:
        //
        // Producer configuration.
        Properties producerProps = new Properties();
        producerProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerHost + ":" + brokerPort);
        producerProps.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "SaasProducer");
        //
        StringSerializer keySerializer = new StringSerializer();
        //
        SchemaRegistryClient mockSchemaRegistryClient = new MockSchemaRegistryClient();
        Map<String, String> kasConfig = new HashMap<>() ;
        kasConfig.put(AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, "io.confluent.kafka.serializers.subject.RecordNameStrategy");
        kasConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "no url is needed because we use mock client");
        // btw: Default is 'auto register schemas=true' so we do not have to register our schemas in the mock schema registry.
        // Create a value serializer instance:
        KafkaAvroSerializer valueSerializer = new KafkaAvroSerializer(mockSchemaRegistryClient, kasConfig);
        //
        // Create a new producer instance.
        producer = new KafkaProducer<>(producerProps, keySerializer, valueSerializer);
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
