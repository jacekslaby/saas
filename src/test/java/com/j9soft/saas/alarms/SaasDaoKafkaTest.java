package com.j9soft.saas.alarms;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;


/*
 * The tests from this class verify whether ProducerRecords are correctly prepared to be send to a Kafka topic
 * when a method of SaasDaoKafka is invoked.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasDaoKafkaTest {

    private static SaasDaoKafkaTestEmbeddedBroker embeddedBroker;
    private static SaasDaoKafkaTestConfiguration testConfig;

    protected SaasDao kafkaDao;

    @BeforeClass
    public static void init() throws IOException {

        // Start an embedded Kafka Server
        //
        embeddedBroker = new SaasDaoKafkaTestEmbeddedBroker();
        embeddedBroker.init();

        // Connect to the embedded Kafka.
        testConfig = new SaasDaoKafkaTestConfiguration();

        embeddedBroker.createTopic(testConfig.getTopicName());
    }

    @AfterClass
    public static void cleanup() {
        testConfig.close();
        embeddedBroker.close();
    }

    @Before
    public void initDao() {
        // Create bean to be tested.
        this.kafkaDao = testConfig.getDao();
    }

    @Test
    public void t1_whenReceivedCreateRequest_itIsSavedToKafka() throws JsonProcessingException {

        TestCreateEntityRequest testEntityRequest = new TestCreateEntityRequest().build();

        kafkaDao.createRequest(testEntityRequest.getRequestObject());
    }

    @Test
    public void t2_whenReceivedDeleteRequest_itIsSavedToKafka() throws JsonProcessingException {

        TestDeleteEntityRequest testEntityRequest = new TestDeleteEntityRequest().build();

        kafkaDao.createRequest(testEntityRequest.getRequestObject());
    }

}
