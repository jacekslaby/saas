package com.j9soft.v1repository.entityrequests;

import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Provider of a producer/consumer connected to Kafka broker.
 */
public class KafkaConnector {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConnector.class);

    private static String GRIT_BOOTSTRAP_SERVERS;
    private static String GRIT_SCHEMA_REGISTRY_URL;
    private static String GRIT_REPOSITORY_NAME;

    private static String TOPIC_NAME__COMMANDS;
    private static String TOPIC_NAME__ENTITIES;

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
        GRIT_BOOTSTRAP_SERVERS = readFromEnv("GRIT_BOOTSTRAP_SERVERS", "kafka:9092");
        logger.info("GRIT_BOOTSTRAP_SERVERS={}", GRIT_BOOTSTRAP_SERVERS);

        GRIT_SCHEMA_REGISTRY_URL = readFromEnv("GRIT_SCHEMA_REGISTRY_URL", "http://schema-registry:8081");
        logger.info("GRIT_SCHEMA_REGISTRY_URL={}", GRIT_SCHEMA_REGISTRY_URL);

        GRIT_REPOSITORY_NAME = readFromEnv("GRIT_REPOSITORY_NAME", "prodxphone-saas");
        logger.info("GRIT_REPOSITORY_NAME={}", GRIT_REPOSITORY_NAME);

        TOPIC_NAME__COMMANDS = GRIT_REPOSITORY_NAME + "-" + "v1-commands-topic";
        TOPIC_NAME__ENTITIES = GRIT_REPOSITORY_NAME + "-" + "v1-entities-topic";
    }

    private static String readFromEnv(String envPropertyName, String valueReturnedIfPropertyDoesNotExist) {
        String envValue = System.getenv(envPropertyName);
        if (envValue != null) {
            return envValue;
        } else {
            return valueReturnedIfPropertyDoesNotExist;
        }
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
        // @TODO use the real schema registry (but first create a test case which will show the problem)
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

    public static KafkaConsumer<String, EntityV1> connectConsumer() {

        // Consumer:
        //
        // Consumer configuration.
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, GRIT_BOOTSTRAP_SERVERS);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "generic-repository-it-producer");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "generic-repository-it-producer");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, GRIT_SCHEMA_REGISTRY_URL);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        // Create a new consumer instance.
        KafkaConsumer<String, EntityV1> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(getEntitiesTopicName()));
        logger.info("Kafka consumer: " + consumer);

        return consumer;
    }

    public static String getCommandsTopicName() {
        return TOPIC_NAME__COMMANDS;
    }

    public static String getEntitiesTopicName() {
        return TOPIC_NAME__ENTITIES;
    }
}
