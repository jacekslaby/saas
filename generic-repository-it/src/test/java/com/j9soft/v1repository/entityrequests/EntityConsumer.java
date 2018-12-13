package com.j9soft.v1repository.entityrequests;

import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EntityConsumer {
    private static final Logger logger = LoggerFactory.getLogger(RequestProducer.class);

    private String topicName;
    private KafkaConsumer<String, EntityV1> consumer;

    /**
     * Note: Autowire - The idea is that it is possible to create a new class annotated as @Configuration
     *  and this class will get autowired here. (btw: In this class it is important to remember about a destroy method to close a producer.)
     */
    // @TODO @Autowired
    public EntityConsumer() {
        this.topicName = KafkaConnector.getEntitiesTopicName();
        this.consumer = KafkaConnector.connectConsumer();
    }

    public void skipOldEntities() {
        this.consumer.seekToEnd( this.consumer.assignment() );
    }

    public List<EntityV1> pollAllNewEntities() {

        List<EntityV1> result = new ArrayList<>();

        ConsumerRecords<String, EntityV1> records = consumer.poll(Duration.ofSeconds(2));
        for (ConsumerRecord<String, EntityV1> record : records) {
            // @TODO remove duplicates, i.e. support for updates
            String key = record.key();
            EntityV1 value = record.value();
            result.add(value);
        }

        return result;
    }

    public List<EntityV1> pollAllExistingEntities() {

        this.consumer.seekToBeginning( this.consumer.assignment() );

        List<EntityV1> result = new ArrayList<>();

        ConsumerRecords<String, EntityV1> records = consumer.poll(Duration.ofSeconds(2));
        for (ConsumerRecord<String, EntityV1> record : records) {
            String key = record.key();
            EntityV1 value = record.value();
            result.add(value);
        }

        return result;
    }
}
