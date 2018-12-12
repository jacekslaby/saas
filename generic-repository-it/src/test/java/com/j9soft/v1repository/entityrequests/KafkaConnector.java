package com.j9soft.v1repository.entityrequests;

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
 * Provider of a producer connected to Kafka broker.
 */
public class KafkaConnector {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConnector.class);

    private static final String BROKER_HOST = "kafka1";
    private static final Integer BROKER_PORT = 9092;
    private static String KAFKA_BOOTSTRAP_SERVERS;

    private static final String TOPIC_NAME__COMMANDS = "v1-commands-topic";

    static {
        // If we want to run 'mvn verify' not from docker-compose. (i.e. when 'kafka1' hostname is not provided)
        //  then we use environment variable to tell where our Kafka broker is.
        //  (e.g. on Windows + DockerMachine in VirtualBox + kafka1 in a container (with exposed public port 9092) we use:
        //      docker-compose --file src/test/resources/docker-compose.yml run -p 9092:9092 -d kafka1
        //      SET KAFKA_BOOTSTRAP_SERVERS=192.168.99.100:9092
        //      mvn verify
        //  )
        //
        String envValue = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        if (envValue != null) {
            KAFKA_BOOTSTRAP_SERVERS = envValue;
        } else {
            KAFKA_BOOTSTRAP_SERVERS = BROKER_HOST + ":" + BROKER_PORT;
        }
    }

    public static KafkaProducer<String, Object> connectProducer() {

        // Producer:
        //
        // Producer configuration.
        Properties producerProps = new Properties();
        producerProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        producerProps.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "generic-repository-it-producer");
        //
        StringSerializer keySerializer = new StringSerializer();
        //
        SchemaRegistryClient mockSchemaRegistryClient = new MockSchemaRegistryClient();
        Map<String, String> kasConfig = new HashMap<>() ;
        kasConfig.put(AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, "io.confluent.kafka.serializers.subject.RecordNameStrategy");
        kasConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, " @TODO ");
        // btw: Default is 'auto register schemas=true' so we do not have to register our schemas in the mock schema registry.
        // Create a value serializer instance:
        KafkaAvroSerializer valueSerializer = new KafkaAvroSerializer(mockSchemaRegistryClient, kasConfig);
        //
        // Create a new producer instance.
        KafkaProducer<String, Object> producer = new KafkaProducer<>(producerProps, keySerializer, valueSerializer);
        logger.info("Kafka producer: " + producer);

        return producer;
    }

    public static String getTopicName() {
        return TOPIC_NAME__COMMANDS;
    }
}
