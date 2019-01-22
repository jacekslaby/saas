package com.j9soft.v1repository.entityrequests;

import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EntityConsumer.class);

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

        // Unnecessary poll() but it is a necessary workaround.
        // (Otherwise consumer does not move to current end offsets right now
        //   and current offsets are lost, which defeats the purpose of the test scenario.)
        //
        // See also: https://stackoverflow.com/questions/28561147/how-to-read-data-using-kafka-consumer-api-from-beginning
        //  " call poll(), then do a seekToBeginning() and then again call poll() if you want all the records from the start. "
        consumer.poll(Duration.ofSeconds(1));
    }

    /**
     * Retrieves the entities published since the last poll.
     * Keeps their order from the topic. Supports tombstones (i.e. delete) and updates.
     */
    public List<EntityV1> pollAllNewEntities() {

        logger.info("pollAllNewEntities: start");
        List<EntityV1> result = pollFromCurrentPosition();
        logger.info("pollAllNewEntities: result.size={}", result.size());
        return result;
    }

    /**
     * Retrieves the existing entities.
     * Keeps their order from the topic. Supports tombstones (i.e. delete) and updates.
     */
    public List<EntityV1> pollAllExistingEntities() {

        logger.info("pollAllExistingEntities: start");

        // Unnecessary poll() but it is a necessary workaround.
        // See also: https://stackoverflow.com/questions/28561147/how-to-read-data-using-kafka-consumer-api-from-beginning
        //  " call poll(), then do a seekToBeginning() and then again call poll() if you want all the records from the start. "
        consumer.poll(Duration.ofSeconds(1));
        this.consumer.seekToBeginning( this.consumer.assignment() );

        List<EntityV1> result = pollFromCurrentPosition();
        logger.info("pollAllExistingEntities: result.size={}", result.size());
        return result;
    }

    private List<EntityV1> pollFromCurrentPosition() {

        List<EntityV1> resultList = new ArrayList<>();
        Map<String, Integer> positions = new HashMap<>();

        ConsumerRecords<String, EntityV1> records = consumer.poll(Duration.ofSeconds(5));
        logger.info("pollAllExistingEntities: records.count={}", records.count());

        for (ConsumerRecord<String, EntityV1> record : records) {
            String key = record.key();
            EntityV1 value = record.value();

            Integer positionOfPreviousEntryWithTheSameEntity = positions.get(key);
            if ( positionOfPreviousEntryWithTheSameEntity != null ) {
                // Set this to be ignored when building result.
                resultList.set(positionOfPreviousEntryWithTheSameEntity, null);
                if (value == null) {
                    // If a tombstone is received it means that this entity was removed
                    positions.remove(key);
                }
            }

            if (value != null) {
                positions.put(key, resultList.size());
                resultList.add(value);
            }
        }

        List<EntityV1> result = new ArrayList<>();
        for (EntityV1 entity: resultList) {
            if (entity != null) {
                result.add(entity);
            }
        }
        logger.info("pollAllExistingEntities: result={}", result);
        return result;
    }

    public void close() {
        if (this.consumer != null) {
            this.consumer.close();
        }
    }
}
