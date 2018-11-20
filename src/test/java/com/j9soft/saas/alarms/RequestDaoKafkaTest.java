package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.dao.RequestDao;
import com.j9soft.saas.alarms.dao.RequestDaoKafka;
import com.j9soft.saas.alarms.testconfig.SaasDaoKafkaTestConfiguration;
import com.j9soft.saas.alarms.testconfig.SaasDaoKafkaTestEmbeddedBroker;
import com.j9soft.saas.alarms.testdata.TestDaoRequestsBuilder;
import com.j9soft.saas.alarms.testdata.TestDtoRequests;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.isNull;


/*
 * The tests from this class verify whether ProducerRecords are correctly prepared
 * and send to a Kafka topic (i.e. to the embedded Kafka instance)
 * when a method of RequestDaoKafka is invoked.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RequestDaoKafkaTest {

    private static SaasDaoKafkaTestEmbeddedBroker embeddedBroker;
    private static SaasDaoKafkaTestConfiguration testConfig;

    protected RequestDaoKafka kafkaDao;
    private RequestDao.Callback callbackMock;
    private TestDaoRequestsBuilder builder;
    private TestDtoRequests testDtoRequests;

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
        kafkaDao = testConfig.getDao();

        callbackMock = Mockito.mock(RequestDao.Callback.class);

        builder = TestDaoRequestsBuilder.newBuilder();
        testDtoRequests = TestDtoRequests.newBuilder(builder);
    }

    @Test
    public void t1_whenReceivedCreateRequest_itIsSavedToKafka() {

        kafkaDao.saveNewRequest(builder.getCreateEntityRequest(), callbackMock);

        // We expect that our callback object receives a null Exception, which means a successful send operation to kafka.
        Mockito.verify(callbackMock, Mockito.timeout(1000)).onCompletion(isNull());
    }

    @Test
    public void t2_whenReceivedDeleteRequest_itIsSavedToKafka() {

        kafkaDao.saveNewRequest(builder.getDeleteEntityRequest(), callbackMock);

        Mockito.verify(callbackMock, Mockito.timeout(1000)).onCompletion(isNull());
    }

    @Test
    public void t3_whenPostedResyncAllStartSubdomainRequest_itIsSavedToDao() {

        kafkaDao.saveNewRequest(builder.getResyncAllStartSubdomainRequest(), callbackMock);

        Mockito.verify(callbackMock, Mockito.timeout(1000)).onCompletion(isNull());
    }

    @Test
    public void t4_whenPostedResyncAllEndSubdomainRequest_itIsSavedToDao() {

        kafkaDao.saveNewRequest(builder.getResyncAllEndSubdomainRequest(), callbackMock);

        Mockito.verify(callbackMock, Mockito.timeout(1000)).onCompletion(isNull());
    }
}
