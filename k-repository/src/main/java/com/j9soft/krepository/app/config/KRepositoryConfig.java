package com.j9soft.krepository.app.config;

import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Properties;

/**
 * Configuration settings are read from system properties.
 */
@Component
public class KRepositoryConfig {

    private static final Logger logger = LoggerFactory.getLogger(KRepositoryConfig.class);

    private static final String COMMANDS_TOPIC_NAME = "v1-commands-topic";
    private static final String ENTITIES_TOPIC_NAME = "v1-entities-topic";

    private String bootstrapServers;
    private String schemaRegistryUrl;
    private String repositoryName;

    @Autowired
    KRepositoryConfig(@Value("${KR_BOOTSTRAP_SERVERS:kafka:9092}") String bootstrapServers,
                      @Value("${KR_SCHEMA_REGISTRY_URL:http://schema-registry:8081}") String schemaRegistryUrl,
                      @Value("${KR_REPOSITORY_NAME}") String repositoryName) {

        if (logger.isInfoEnabled()) {
            logger.info("Connecting to Kafka with system properties: {}", Arrays.asList(
                    System.getProperties().entrySet().stream()
                            .filter(e -> ((String) e.getKey()).startsWith("KR_"))
                            .toArray())
            );
            logger.info("Connecting to Kafka at: {}, Schema Registry at: {}, using Repository Name (group.id and topics' prefix): {}",
                    bootstrapServers, schemaRegistryUrl, repositoryName);
        }

        if (repositoryName == null) {
            // @TODO better exception handling, new class Errors.
            throw new RuntimeException();
        }

        this.bootstrapServers = bootstrapServers;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.repositoryName = repositoryName;
    }

    public String getCommandsTopicName() {
        if (repositoryName != null) {
            return repositoryName + "-" + COMMANDS_TOPIC_NAME;
        } else {
            return COMMANDS_TOPIC_NAME;
        }
    }

    public String getEntitiesTopicName() {
        if (repositoryName != null) {
            return repositoryName + "-" + ENTITIES_TOPIC_NAME;
        } else {
            return ENTITIES_TOPIC_NAME;
        }
    }

    public Properties getStreamsProperties() {
        Properties props = new Properties();

        // Setting configuration according to:
        //   https://kafka.apache.org/21/documentation/streams/developer-guide/config-streams
        //

        // The application ID. Each stream processing application must have a unique ID.
        // This ID is used in the following places to isolate resources used by the application from others:
        //
        // As the default Kafka consumer and producer client.id prefix
        // As the Kafka consumer group.id for coordination
        // As the name of the subdirectory in the state directory (cf. state.dir)
        // As the prefix of internal Kafka topic names
        //
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "k-repository-" + this.repositoryName);

        // The Kafka bootstrap servers. This is the same setting that is used by the underlying producer and consumer clients to connect to the Kafka cluster.
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);

        // We want to skip unknown requests, i.e. those not matching avro schemas.
        // ( https://cwiki.apache.org/confluence/display/KAFKA/KIP-161%3A+streams+deserialization+exception+handlers )
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                LogAndContinueExceptionHandler.class.getName());

        // @TODO Replication should be 3. (in order to support one failure during an ongoing maintenance of one broker)
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);

        return props;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }
}

