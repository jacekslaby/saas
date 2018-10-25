package com.j9soft.saas.alarms;

import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasDaoKafkaTest {

    private static RaasDaoKafkaTestEmbeddedBroker embeddedBroker;

    private static RaasDaoKafkaTestConfiguration testConfig;
    protected SaasDao kafkaDao;
    private RaasDaoTestScenarios scenarios;

    @BeforeClass
    public static void init() throws IOException {

        // Start an embedded Kafka Server
        //
        embeddedBroker = new RaasDaoKafkaTestEmbeddedBroker();
        embeddedBroker.init();

        // Connect to the embedded Kafka.
        testConfig = new RaasDaoKafkaTestConfiguration();

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
        scenarios = new RaasDaoTestScenarios(this.kafkaDao);
    }

    @Test
    public void t1_todo() {
        scenarios.t1_todo();
    }

}
