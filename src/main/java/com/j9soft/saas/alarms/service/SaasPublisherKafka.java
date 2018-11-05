package com.j9soft.saas.alarms.service;

import com.j9soft.saas.alarms.dao.SaasDaoKafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementation of publisher layer based on a Kafka DAO.
 *
 * This publisher Service is used in production mode, i.e. in production environments.
 */
@Profile({"prod","kafka-dev"})
@Service
public class SaasPublisherKafka implements SaasPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SaasPublisherKafka.class);

    private SaasDaoKafka saasDaoKafka;

    /**
     * Note: Autowire - The idea is that it is possible to create a new class annotated as @Configuration
     *  and this class will get autowired here. (btw: In this class it is important to remember about a destroy method to close a producer.)
     */
    @Autowired
    public SaasPublisherKafka(SaasDaoKafka saasDaoKafka) {

        this.saasDaoKafka = saasDaoKafka;
    }

    @Override
    public void publishRequest(SaasPublisher.Request request) {

        request.accept(saasDaoKafka);

        // TODO How to report exceptions to a client ?  When and how it is possible to discover any problems with send() ?
        // We need to register a callback. See TODOs described in the method publishRequestsWithArray() below.
    }

    @Override
    public void publishRequestsWithArray(SaasPublisher.Request[] requests) {

        // Note: In case when our Publisher publishes more than one request (i.e. publishRequestsWithArray() )
        //  it is quite likely that they are send to different partitions. (because our topic is partitioned by hash of request.uuid)
        //  It is not a problem (as long as there are no exceptions) because kafka guarantee is per partition:
        //    "Idempotence: Exactly-once in order semantics per partition"
        // However, in case of an exception, it means that it may happen that only a subset of requests is saved in kafka.
        //  (And we cannot avoid it because otherwise we would have to use: "Transactions: Atomic writes across multiple partitions".)
        // This means that REST clients of SaasApplication must be ready for these exceptions
        //  and the only thing they can do is to force a full resynchronization.
        // (However, it is not an "ordinary" situation, so we do not want to sacrifice performance here
        //   in order to ease development of REST clients.)
        //
        // See also:
        //
        // https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/
        // - "to get exactly-once semantics per partition - meaning no duplicates, no data loss, and in-order semantics -
        //    configure your producer to set “enable.idempotence=true”
        // - "It is worth noting that a Kafka topic partition might have some messages that are part of a transaction
        //     while others that are not.
        //     (So on the Consumer side: read_committed: In addition to reading messages that are not part of a transaction,
        //        also be able to read ones that are, after the transaction is committed.)
        //    "
        //
        // https://www.confluent.io/blog/transactions-apache-kafka/
        //  "We designed transactions in Kafka primarily for applications which exhibit a “read-process-write” pattern
        //  where the reads and writes are from and to asynchronous data streams such as Kafka topics."
        //

        // @TODO think over - perhaps OpenAPI should not allow to mix entity and subdomain requests ?
        //   This way clients would have an easier recovery path in case an exception happens for a publishRequestsWithArray.
        //   Assuming some requests went through without exceptions and some with exceptions
        //   a smart client code (which did not mix same entityId in the array) would be able to retry just those with exceptions.
        //  (however - does it make sense ? Kafka will provide own retries (TODO configure it),
        //    so what is the purpose for a client to try again on its own ? Hm... maybe in 1h kafka will be up again ?)

        // TODO How to report exceptions to a client ?  When and how it is possible to discover any problems with send() ?
        //
        // Register one callback object which groups these requests together
        // and returns a response after they are send.
        // See how it is done at "new ProducerPool.ProduceRequestCallback() " in
        // https://github.com/confluentinc/kafka-rest/blob/master/kafka-rest/src/main/java/io/confluent/kafkarest/resources/PartitionsResource.java
        // (but we do not need a pool of producers, because we have only one type,  (in kafka-rest they have several types: avro, no-schema,etc.)
        //  and it is thread safe)
        //
        // "Container for state associated with one REST-ful produce request, i.e. a batched send"
        // https://github.com/confluentinc/kafka-rest/blob/master/kafka-rest/src/main/java/io/confluent/kafkarest/ProduceTask.java

        // TODO AsyncResponse ?  (to improve performance/parallelizm ?)

        for (SaasPublisher.Request request: requests) {
            request.accept(saasDaoKafka);
        }
    }
}
