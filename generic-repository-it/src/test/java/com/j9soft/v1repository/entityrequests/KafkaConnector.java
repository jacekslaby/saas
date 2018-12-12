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

    private static final String BROKER_HOST = "kafka";
    private static final Integer BROKER_PORT = 29092;
    private static String GRIT_BOOTSTRAP_SERVERS;

    private static final String TOPIC_NAME__COMMANDS = "v1-commands-topic";

    static {
        // If we want to run 'mvn verify' not from docker-compose. (i.e. when 'kafka1' hostname is not provided)
        //  then we use environment variable to tell where our Kafka broker is.
        //  (e.g. on Windows + DockerMachine in VirtualBox (with IP 192.168.99.100) + kafka in a container (with exposed public port 29092) we use:
        //      docker-compose --file src/test/resources/docker-compose.yml run -p 29092:9092 -d kafka
        //      SET GRIT_BOOTSTRAP_SERVERS=192.168.99.100:29092
        //      mvn verify
        //
        //      (and you need /etc/hosts: 192.168.99.100   kafka # because broker returns its DNS name 'kafka' and producer connects to it)
        //  )
        /*  (Hm.... probably easier is to just do:
               mvn docker:build
               docker-compose --file src/test/resources/docker-compose.yml  run --rm  generic-repository-it

        //   It is a bit longer, but much simpler.  (the trouble is that we loose IDE help, e.g. exception stack trace navigation...)
        //  )
        */
        String envValue = System.getenv("GRIT_BOOTSTRAP_SERVERS");
        if (envValue != null) {
            GRIT_BOOTSTRAP_SERVERS = envValue;
        } else {
            GRIT_BOOTSTRAP_SERVERS = BROKER_HOST + ":" + BROKER_PORT;
        }

        logger.info("GRIT_BOOTSTRAP_SERVERS={}", GRIT_BOOTSTRAP_SERVERS);
    }

    public static KafkaProducer<String, Object> connectProducer() {

        // Producer:
        //
        // Producer configuration.
        Properties producerProps = new Properties();
        producerProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, GRIT_BOOTSTRAP_SERVERS);
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
