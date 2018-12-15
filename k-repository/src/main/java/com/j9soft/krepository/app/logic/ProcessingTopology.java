package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.app.config.KRepositoryConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProcessingTopology {

    public static final String LOCAL_STORE_NAME = "entities-store";

    private KRepositoryConfig config;

    @Autowired
    public ProcessingTopology(KRepositoryConfig config) {
        this.config = config;

        // Let's prepare serializers/deserializers to be used when reading from and writing to topics.
        //
        // https://docs.confluent.io/current/streams/developer-guide/datatypes.html#streams-developer-guide-serdes
        //
        // Our key is just the same string as in property entity_id_in_subdomain.
        final Serde<String> keySerde = Serdes.String();

        // Our values encoded as AVRO schemas so we need to configure a Serde.
        //
        final Map<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put("schema.registry.url", config.getSchemaRegistryUrl());
        // We have may types (avro schemas) used on both topics,
        //  so we need to tell the serializers/deserializers that fact.
        // (see also:
        //   http://martin.kleppmann.com/2018/01/18/event-types-in-kafka-topic.html
        //     serdeConfig.put("value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy");
        //   https://stackoverflow.com/questions/51429759/multiple-message-types-in-a-single-kafka-topic-with-avro
        //     "I haven't seen any working example of this. Not even a single one."
        // )
        serdeConfig.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class.getName());
        //
        final Serde<GenericRecord> commandSerde = new GenericAvroSerde();
        commandSerde.configure(serdeConfig, false); // `false` for record values
        //
        final Serde<GenericRecord> entitySerde = new GenericAvroSerde();
        commandSerde.configure(serdeConfig, false); // `false` for record values

        // Let's prepare our k-streams processing Topology:
        //  v1-commands-topic -> join -> store (KTable based on v1-entities-topic)
        //       to calculate a new entity (with attributes merged from value provided from v1-commands-topic and v1-entities-topic)
        //  store -> calculate new entity value -> publish on v1-entities-topic
        //
        StreamsBuilder builder = new StreamsBuilder();

        // Input stream with requests to be executed on entities.
        // Based on: v1-commands-topic
        // Key is entity_id_in_subdomain - this way
        // (Note: for subdomain requests key must be not null,
        //   but its value is irrelevant. (and producers must assure that a copy of every subdomain request
        //    is published to every partition of v1-commands-topic, i.e. they should not use key based partitioner)
        //
        KStream<String, GenericRecord> commandsStream = builder.stream( config.getCommandsTopicName(),
                Consumed.with(keySerde, commandSerde));

        // Input table with current entity values.
        // Based on: v1-entities-topic
        //
        // https://kafka.apache.org/21/documentation/streams/developer-guide/dsl-api
        // "the local KTable instance of every application instance will be populated with data
        //   from only a subset of the partitions of the input topic."
        //
        // @TODO You must provide a name for the table (more precisely, for the internal state store that backs the table). This is required for supporting interactive queries against the table. When a name is not provided the table will not queryable and an internal name will be provided for the state store.
        //
        KTable<String, GenericRecord> currentEntitiesTable = builder.table( config.getEntitiesTopicName(),
                Consumed.with(keySerde, entitySerde));

        // For every received Let's perform lookup
        //
        KStream<String, GenericRecord> newEntitiesStream =
                // https://kafka.apache.org/21/documentation/streams/developer-guide/dsl-api
                // "KTable also provides an ability to look up current values of data records by keys.
                //  This table-lookup functionality is available through join operations
                //
                //  leftJoin:
                //  Performs a LEFT JOIN of this stream with the table, effectively doing a table lookup.
                //  Input records with a null key or a null value are ignored and do not trigger the join."
                // Note: because of the above we must assure that also subdomain requests have a key.
                //
                commandsStream.leftJoin(currentEntitiesTable,
                        new CommandExecutor()); // the user-supplied ValueJoiner will be called to produce join output records.

        // Publish the new values to the same topic, v1-entities-topic.
        //
        // Note: This is only a draft implementation. It does not guarantee consistency
        //  as our oldEntitiesTable may be delayed.  (i.e. the latest state of an entity may not be in memory yet)
        // @TODO The target implementation needs to be done using local state store.
        //   (and using Processor API - in order to avoid significant overhead introduced by KStreams (i.e. those lots of intermediate topics))
        //
        newEntitiesStream.to(config.getEntitiesTopicName(), Produced.with(keySerde, entitySerde));

        // Start the Kafka Streams threads
        //
        // https://kafka.apache.org/21/documentation/streams/developer-guide/write-streams.html
        //
        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, config.getStreamsProperties());
        streams.start();
    }

    /*
    najprościej będzie to zaimplementować w KStreams.

Ale dopisać że efektywniejsze będzie zrobić samemu, zgodnie z:
https://aseigneurin.github.io/2017/08/04/why-kafka-streams-didnt-work-for-us-part-3.html
bo: nie będzie tych zbędnych topic'ów.

    public ProcessingTopology() {

        // Relevant info:
        //  https://kafka.apache.org/documentation/streams/architecture#streams_architecture_tasks
        //  https://docs.confluent.io/current/streams/developer-guide/processor-api.html#connecting-processors-and-state-stores
        //  https://github.com/bbejeck/kafka-streams/blob/master/src/main/java/bbejeck/streams/purchases/PurchaseKafkaStreamsDriver.java
        //  https://github.com/confluentinc/online-inferencing-blog-application/blob/master/src/main/java/org/apache/kafka/inference/blog/streams/KStreamsOnLinePredictions.java
        //

        Topology topology = new Topology();

        // https://docs.confluent.io/current/streams/developer-guide/processor-api.html#enable-or-disable-fault-tolerance-of-state-stores-store-changelogs
        Map<String, String> changelogConfig = new HashMap<>();
        // override min.insync.replicas
        changelogConfig.put("min.insyc.replicas", "2");

        // https://docs.confluent.io/current/streams/developer-guide/datatypes.html#streams-developer-guide-serdes
        StoreBuilder<KeyValueStore<String, Long>> lastStateStoreBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(LOCAL_STORE_NAME),
                Serdes.String(),
                Serdes.Long())
                .withLoggingEnabled(changelogConfig); // enable changelogConfig, with custom changelog settings

        // add the source processor node (named "Commands") that takes Kafka topic "v1-commands-topic" as input
        topology.addSource("Commands", this.config.getCommandsTopicName()) // Consumed.with(StringSerde, valueGenericAvroSerde)
                // add the CommandProcessor node which takes the source processor as its upstream processor
                .addProcessor("CommandProcessor", () -> new CommandProcessor(), "Commands")
                // add the count store associated with the CommandProcessor processor
                .addStateStore(lastStateStoreBuilder, "CommandProcessor")
                // add the sink processor node (named "Entities") that takes Kafka topic "v1-entities-topic" as output
                // and the CommandProcessor node as its upstream processor
                .addSink("Entities", this.config.getEntitiesTopicName(), "CommandProcessor");

        // Use the topology and streamingConfig to start the kafka streams processing
        KafkaStreams streaming = new KafkaStreams(topology, config.getStreamsProperties());
        streaming.start();
    }
     */
}
