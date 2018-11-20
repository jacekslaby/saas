package com.j9soft.saas.alarms.dao;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllEndSubdomainRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllStartSubdomainRequestV1;
import com.j9soft.saas.alarms.config.KafkaConnector;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementation of data access layer (DAO) based on a Kafka producer.
 *
 * This Dao is used in production mode, i.e. in production environments.
 *
 * Note: We do NOT use transactions because that would be a significant performance penalty.
 *   We would have to synchronize and basically serialize all requests incoming to SaasV1Controller. Not nice.
 *   The reason: "there can be only _one_open_transaction_ per producer"
 *    https://kafka.apache.org/20/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
 *
 * Note: We do not synchronize invocations of producer.send() because:
 *  "The producer is thread safe and sharing a single producer instance across threads will generally be faster than having multiple instances.".
 *  https://kafka.apache.org/20/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
 */
@Profile({"prod","kafka-dev"})
@Service
public class RequestDaoKafka implements RequestDao {

    private String topicName;
    private KafkaProducer<String, Object> producer;

    /**
     * Note: Autowire - The idea is that it is possible to create a new class annotated as @Configuration
     *  and this class will get autowired here. (btw: In this class it is important to remember about a destroy method to close a producer.)
     */
    @Autowired
    public RequestDaoKafka(KafkaConnector connector) {
        this.topicName = connector.getTopicName();
        this.producer = connector.getProducer();
    }

    @Override
    public void saveNewRequest(CreateEntityRequestV1 request, RequestDao.Callback callback) {

        // @TODO think over the idea of partitioning by uuid    (because most likely all requests from an array are from one subdomain,
        //   so it makes little sense to commit to several partitions which is probably more costly as they may reside on different kafka brokers)
        // Perhaps partitioning by subdomain is better.  (but there is a problem of quiet vs talkative adapters -
        //  the idea was to balance them via partitioning by uuid. And later republishing to an internal topic partitioned by subpartition,
        //   where several small adapters may be in one subpartition  and one big adapter may be in several subpartitions.
        //   SubdomainRequests would need to be duplicated to all such subpartitions of this big adapter.)
        //

        // Note: Callback object is used to report exceptions to a client. (in case any problems are discovered with send())
        // Implementation details:
        //  Register one object (PublishTask) which groups these requests together, receives callbacks from send(),
        //   and returns a response after they are send.
        //  See how it is done at:
        //  - "new ProducerPool.ProduceRequestCallback() "
        //    https://github.com/confluentinc/kafka-rest/blob/master/kafka-rest/src/main/java/io/confluent/kafkarest/resources/PartitionsResource.java
        //    (but here we do not need a pool of producers, because we have only one type,  (in kafka-rest they have several types: avro, no-schema,etc.)
        //    and it is thread safe)
        //  - "Container for state associated with one REST-ful produce request, i.e. a batched send"
        //    https://github.com/confluentinc/kafka-rest/blob/master/kafka-rest/src/main/java/io/confluent/kafkarest/ProduceTask.java


        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record, (recordMetadata, e) -> callback.onCompletion(e));
    }

    @Override
    public void saveNewRequest(DeleteEntityRequestV1 request, RequestDao.Callback callback) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record, (recordMetadata, e) -> callback.onCompletion(e));
    }

    @Override
    public void saveNewRequest(ResyncAllStartSubdomainRequestV1 request, RequestDao.Callback callback) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record, (recordMetadata, e) -> callback.onCompletion(e));
    }

    @Override
    public void saveNewRequest(ResyncAllEndSubdomainRequestV1 request, RequestDao.Callback callback) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(this.topicName, request.getUuid().toString(), request);
        this.producer.send(record, (recordMetadata, e) -> callback.onCompletion(e));
    }
}
