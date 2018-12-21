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
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProcessingTopology {

    public static final String LOCAL_STORE_NAME = "entities-store";

    @Autowired
    public ProcessingTopology(KRepositoryConfig config) {

        Map<String, String> avroSerdeConfig = prepareConfigPropertiesOfAvroSerde( config.getSchemaRegistryUrl() );

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

        // Topics have messages with different values, so we need different Serdes.
        final Serde<SpecificRecord> commandSerde = createValueSerde(avroSerdeConfig); // SpecificRecord because this topic has different classes as values
        final Serde<EntityV1> entitySerde = createValueSerde(avroSerdeConfig);

        // Let's prepare our k-streams processing Topology as follows:
        //  (input) v1-commands-topic -> join to the local store (which is implemented as a KTable based on v1-entities-topic)
        //  join: During the join we calculate a new entity. (with attributes merged from value provided from v1-commands-topic and v1-entities-topic)
        //  join -> update in the local store (what is automatically stored in v1-entities-topic by the KTable logic)
        //  join -> publish on v1-entities-topic  (output)
        //             (i.e. currently we do publish manually to v1-entities-topic,
        //               but in the @FUTURE it will be automatically saved by a logic of the local store)
        //
        StreamsBuilder builder = new StreamsBuilder();

        // Input stream with requests to be executed on entities.
        // Based on: v1-commands-topic
        // Key is entity_id_in_subdomain - this way it is possible to join it with KTable based on v1-entities-topic.
        // (Note: for subdomain requests key must be not null,
        //   but key content is irrelevant. (additionally producers must assure that a copy of every subdomain request
        //    is published to every partition of v1-commands-topic, i.e. they should not use a key based partitioner)
        //
        KStream<String, SpecificRecord> commandsStream = builder.stream( config.getCommandsTopicName(),
                Consumed.with(keySerde, commandSerde));

        // Input table with current entity values.
        // Based on: v1-entities-topic
        // Contains only some of the entities - only those from topic partitions relevant to this instance of k-repository app.
        // https://kafka.apache.org/21/documentation/streams/developer-guide/dsl-api
        // "the local KTable instance of every application instance will be populated with data
        //   from only a subset of the partitions of the input topic."
        //
        // @TODO You must provide a name for the table (more precisely, for the internal state store that backs the table). This is required for supporting interactive queries against the table. When a name is not provided the table will not queryable and an internal name will be provided for the state store.
        //
        KTable<String, EntityV1> currentEntitiesTable = builder.table( config.getEntitiesTopicName(),
                Consumed.with(keySerde, entitySerde));

        // For every received request let's perform lookup for a current Entity value, i.e. a left join.
        //
        KStream<String, EntityV1> newEntitiesStream =
                // https://kafka.apache.org/21/documentation/streams/developer-guide/dsl-api
                // "KTable also provides an ability to look up current values of data records by keys.
                //  This table-lookup functionality is available through join operations
                //  leftJoin:
                //  Performs a LEFT JOIN of this stream with the table, effectively doing a table lookup.
                //  Input records with a null key or a null value are ignored and do not trigger the join."
                //
                // Note: because of the above we must assure that also subdomain requests have a key. (although it is enough to be not null)
                // Note: key of messages with entity requests must contain entity_id_in_subdomain ! (otherwise join does not work)
                //
                commandsStream.leftJoin(currentEntitiesTable,
                        new CommandExecutor()); // the ValueJoiner is to produce join output records, i.e. the resulting Entity values.

        // Publish the new Entity values to the same topic, v1-entities-topic.
        // (but first, via flatMap(), we need to ignore some values (e.g. ALREADY_EXISTING_ENTITY_TO_BE_IGNORED)
        //
        // Note: This is only a draft implementation. It does not guarantee consistency
        //  as our currentEntitiesTable may be delayed.  (i.e. the latest state of an entity may not be in memory yet,
        //    due to required cycle: publish + poll (receive) + update KTable.
        // @FUTURE The target implementation needs to be done using a local state store.
        //   (and using Processor API - in order to avoid significant overhead introduced by KStreams (i.e. those lots of intermediate topics))
        //
        newEntitiesStream
                .flatMap(new EntityKeyValueMapper())
                .to(config.getEntitiesTopicName(), Produced.with(keySerde, entitySerde));

        // Start the Kafka Streams threads
        // ( https://kafka.apache.org/21/documentation/streams/developer-guide/write-streams.html )
        //
        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, config.getStreamsProperties());
        streams.start();
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

    /*
    Note: Current implementation, provided above, is using Kafka Streams.
      It is well enough for PoC (and it was much quicker to develop),
       but for the production code we would use Processor API in order to:
        - decrease resource usage.  see more: https://aseigneurin.github.io/2017/08/04/why-kafka-streams-didnt-work-for-us-part-3.html
       -  avoid stale reads from KTable due to publish+poll cycle)

    public ProcessingTopology() {

        // Relevant info:
        //  https://kafka.apache.org/documentation/streams/architecture#streams_architecture_tasks
        //  https://docs.confluent.io/current/streams/developer-guide/processor-api.html#connecting-processors-and-state-stores
        //  https://github.com/bbejeck/kafka-streams/blob/master/src/main/java/bbejeck/streams/purchases/PurchaseKafkaStreamsDriver.java
        //  https://github.com/confluentinc/online-inferencing-blog-application/blob/master/src/main/java/org/apache/kafka/inference/blog/streams/KStreamsOnLinePredictions.java
        //  http://codingjunkie.net/kafka-processor-part1/

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
