package com.j9soft.saas.alarms;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;


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
    public void whenReceivedCreateRequest_itIsSavedToKafka() throws JsonProcessingException {

        TestCreateEntityRequest testCreateEntityRequest = new TestCreateEntityRequest().build();

        kafkaDao.createRequest(testCreateEntityRequest.getRequestObject());
    }

}
