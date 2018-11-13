package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.dao.SaasDao;
import com.j9soft.saas.alarms.dao.SaasDaoKafka;
import com.j9soft.saas.alarms.testconfig.SaasDaoKafkaTestConfiguration;
import com.j9soft.saas.alarms.testconfig.SaasDaoKafkaTestEmbeddedBroker;
import com.j9soft.saas.alarms.testdata.TestCreateEntityRequest;
import com.j9soft.saas.alarms.testdata.TestDeleteEntityRequest;
import com.j9soft.saas.alarms.testdata.TestResyncAllEndSubdomainRequest;
import com.j9soft.saas.alarms.testdata.TestResyncAllStartSubdomainRequest;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.isNull;


/*
 * The tests from this class verify whether ProducerRecords are correctly prepared
 * and send to a Kafka topic (i.e. to the embedded Kafka instance)
 * when a method of SaasDaoKafka is invoked.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasDaoKafkaTest {

    private static SaasDaoKafkaTestEmbeddedBroker embeddedBroker;
    private static SaasDaoKafkaTestConfiguration testConfig;

    protected SaasDaoKafka kafkaDao;
    private SaasDao.Callback callbackMock;

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

        callbackMock = Mockito.mock(SaasDao.Callback.class);
    }

    @Test
    public void t1_whenReceivedCreateRequest_itIsSavedToKafka() {

        TestCreateEntityRequest testEntityRequest = TestCreateEntityRequest.build();
        kafkaDao.createRequest(testEntityRequest.getRequestObject(), callbackMock);

        // We expect that our callback object receives a null Exception, which means a successful send operation to kafka.
        Mockito.verify(callbackMock, Mockito.timeout(1000)).onCompletion(isNull());
    }

    @Test
    public void t2_whenReceivedDeleteRequest_itIsSavedToKafka() {

        TestDeleteEntityRequest testEntityRequest = TestDeleteEntityRequest.build();
        kafkaDao.createRequest(testEntityRequest.getRequestObject(), callbackMock);

        Mockito.verify(callbackMock, Mockito.timeout(1000)).onCompletion(isNull());
    }

    @Test
    public void t3_whenPostedResyncAllStartSubdomainRequest_itIsSavedToDao() {

        TestResyncAllStartSubdomainRequest testRequest = TestResyncAllStartSubdomainRequest.build();
        kafkaDao.createRequest(testRequest.getRequestObject(), callbackMock);

        Mockito.verify(callbackMock, Mockito.timeout(1000)).onCompletion(isNull());
    }

    @Test
    public void t4_whenPostedResyncAllEndSubdomainRequest_itIsSavedToDao() {

        TestResyncAllEndSubdomainRequest testRequest = TestResyncAllEndSubdomainRequest.build();
        kafkaDao.createRequest(testRequest.getRequestObject(), callbackMock);

        Mockito.verify(callbackMock, Mockito.timeout(1000)).onCompletion(isNull());
    }
}
