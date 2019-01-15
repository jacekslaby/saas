package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.app.config.KRepositoryConfig;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProcessingTopology {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingTopology.class);

    private static final String LOCAL_STORE_NAME = "entities-store";

    @Autowired
    public ProcessingTopology(KRepositoryConfig config) {

        // Let's prepare serializers/deserializers to be used when reading from and writing to topics.
        // ( https://docs.confluent.io/current/streams/developer-guide/datatypes.html#streams-developer-guide-serdes )
        //
        // Our key is just the same string as in property 'entity_id_in_subdomain' of a request object. (e.g. NotificationIdentifier of a SourceAlarm)
        final Serde<String> keySerde = Serdes.String();
        //
        // @FUTURE Entities topic needs avro serde for keys.
        // @FUTURE On entities topic the message key needs to be built from entity_subdomain_name + entity_id_in_subdomain.
        //  (needed in case when different environments (e.g. prod, ref, test) (or clientA, clientB, multitenancy)
        //   use the same topics)

        // Both topics have messages with different values, so we need different Serdes.
        Map<String, String> avroSerdeConfig = prepareConfigPropertiesOfAvroSerde( config.getSchemaRegistryUrl() );
        final Serde<SpecificRecord> commandSerde = createValueSerde(avroSerdeConfig); // SpecificRecord because this topic has different classes as values
        final Serde<EntityV1> entitySerde = createValueSerde(avroSerdeConfig);

        // Let's prepare a local store for keeping current EntityV1 values in memory.
        //  https://docs.confluent.io/current/streams/developer-guide/datatypes.html#streams-developer-guide-serdes
        //  http://mkuthan.github.io/blog/2017/11/02/kafka-streams-dsl-vs-processor-api/
        //
        final Map<String, String> changelogConfig = new HashMap<>();
        //
        // @FUTURE Let's enable fault tolerance for our State Stores. It is based on both: changelog topic and in-memory replicas.
        // https://docs.confluent.io/current/streams/developer-guide/processor-api.html#enable-or-disable-fault-tolerance-of-state-stores-store-changelogs
        // https://docs.confluent.io/current/streams/developer-guide/config-streams.html#streams-developer-guide-standby-replicas
        //  "If you configure n standby replicas, you need to provision n+1 KafkaStreams instances"  (i.e. we need to adjust setup of Integration Test scenarios)
        //changelogConfig.put("min.insync.replicas", "2");
        //
        StoreBuilder<KeyValueStore<String, EntityV1>> lastStateStoreBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(LOCAL_STORE_NAME),
                keySerde,
                entitySerde)
                .withLoggingEnabled(changelogConfig); // enable a changelog for any changes made to the store, with custom changelog settings

        // Let's prepare the processing topology.
        // Relevant info:
        //  https://dzone.com/articles/kafka-streams-catching-data-in-the-act-3-the-mecha
        //  https://aseigneurin.github.io/2017/08/04/why-kafka-streams-didnt-work-for-us-part-3.html
        //  https://kafka.apache.org/documentation/streams/architecture#streams_architecture_tasks
        //  https://docs.confluent.io/current/streams/developer-guide/processor-api.html#connecting-processors-and-state-stores
        //  https://github.com/bbejeck/kafka-streams/blob/master/src/main/java/bbejeck/streams/purchases/PurchaseKafkaStreamsDriver.java
        //  https://github.com/confluentinc/online-inferencing-blog-application/blob/master/src/main/java/org/apache/kafka/inference/blog/streams/KStreamsOnLinePredictions.java
        //  http://codingjunkie.net/kafka-processor-part1/
        //
        Topology topology = new Topology();
        //
        // Our processing topology is as follows:
        //         1. the source processor node (named "Commands") that takes Kafka topic "v1-commands-topic" as input
        topology.addSource("Commands", keySerde.deserializer(), commandSerde.deserializer(), config.getCommandsTopicName() )
                // 2. the CommandProcessor node which takes the source processor as its upstream processor
                .addProcessor("CommandProcessor", () -> new CommandProcessor(LOCAL_STORE_NAME), "Commands")
                // 3. the store associated with the CommandProcessor processor, i.e. the store to persist current Entities
                .addStateStore(lastStateStoreBuilder, "CommandProcessor")
                // 4. the sink processor node (named "Entities") that takes Kafka topic "v1-entities-topic" as output
                //    with the CommandProcessor node as its upstream processor
                .addSink("Entities", config.getEntitiesTopicName(), keySerde.serializer(), entitySerde.serializer(), "CommandProcessor");


        // Start the Kafka Streams threads
        // ( https://kafka.apache.org/21/documentation/streams/developer-guide/write-streams.html )
        //
        KafkaStreams streams = new KafkaStreams(topology, config.getStreamsProperties());
        streams.start();

        // print the topology
        logger.info("Topology: {}", topology.describe());

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static <T extends SpecificRecord> Serde<T> createValueSerde(Map<String, String> avroSerdeConfig) {
        final Serde<T> newSerde = new SpecificAvroSerde<>();
        //
        // We must call configure.
        // (see also https://github.com/confluentinc/kafka-streams-examples/blob/5.0.1-post/src/test/java/io/confluent/examples/streams/SpecificAvroIntegrationTest.java )
        newSerde.configure(avroSerdeConfig, false); // `false` because this Serde is for record/message values, not keys

        return newSerde;
    }

    private Map<String, String> prepareConfigPropertiesOfAvroSerde(String schemaRegistryUrl) {

        // Our values are encoded as Avro schemas objects so we need to configure a Serde with an access to Schema Registry.
        //
        final Map<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        // We do not want to auto register schemas. The schemas are registered by maintenance scripts launched directly against Kafka cluster.
        serdeConfig.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "false");

        // We have may several types (i.e. Avro schemas) used on both topics,
        //  so we need to tell the serializers/deserializers that fact.
        // (see also:
        //   http://martin.kleppmann.com/2018/01/18/event-types-in-kafka-topic.html
        //     serdeConfig.put("value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy");
        //   https://stackoverflow.com/questions/51429759/multiple-message-types-in-a-single-kafka-topic-with-avro
        //     "I haven't seen any working example of this. Not even a single one."
        // )
        serdeConfig.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class.getName());

        // We want to receive POJOs (e.g. EntityV1), so we cannot use GenericAvroSerde.
        //  (see also: https://dzone.com/articles/kafka-avro-serialization-and-the-schema-registry
        //    https://stackoverflow.com/questions/31207768/generic-conversion-from-pojo-to-avro-record
        //  )
        // Instead we have:
        serdeConfig.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");

        return serdeConfig;
    }

}
