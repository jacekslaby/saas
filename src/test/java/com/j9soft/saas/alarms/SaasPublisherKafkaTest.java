package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.dao.RequestDao;
import com.j9soft.saas.alarms.dao.SaasDaoKafka;
import com.j9soft.saas.alarms.service.PublishTask;
import com.j9soft.saas.alarms.service.SaasPublisher;
import com.j9soft.saas.alarms.service.SaasPublisherKafka;
import com.j9soft.saas.alarms.testdata.TestDaoRequestsBuilder;
import com.j9soft.saas.alarms.testdata.TestDtoRequests;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * The tests from this class verify
 * whether expected operations are executed on SaasDaoKafka instance
 * as a result of a request to SaasPublisherKafka.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasPublisherKafkaTest {

    private SaasPublisher saasPublisher;
    private SaasDaoKafka saasDaoMock;
    private PublishTask publishTaskMock;
    private RequestDao.Callback callbackMock;
    private TestDaoRequestsBuilder builder;
    private TestDtoRequests testDtoRequests;

    @Before
    public void initRaas() {

        // Let's create a Dao mock which will be checked for expected operation calls.
        //
        saasDaoMock = Mockito.mock(SaasDaoKafka.class);

        // Let's create a PublishTask mock which will be checked for expected operation calls.
        //
        callbackMock = Mockito.mock(RequestDao.Callback.class);
        publishTaskMock = Mockito.mock(PublishTask.class);
        when(publishTaskMock.createCallback()).thenReturn(callbackMock);

        // Let's create the tested bean.
        saasPublisher = new SaasPublisherKafka(this.saasDaoMock);

        builder = TestDaoRequestsBuilder.newBuilder();
        testDtoRequests = TestDtoRequests.newBuilder(builder);
    }

    @Test
    public void t1_whenPostedCreateAlarmRequest_itIsSavedToDao() {

        // Let's publish a create entity request.
        saasPublisher.publishRequest(publishTaskMock, testDtoRequests.getCreateAlarmRequestDto());

        // Let's verify that it was saved in Dao:
        //
        verify(saasDaoMock).saveNewRequest(same(builder.getCreateEntityRequest()), same(callbackMock));
    }

    @Test
    public void t2_whenPostedDeleteAlarmRequest_itIsSavedToDao() {

        // Let's publish a delete entity request.
        saasPublisher.publishRequest(publishTaskMock, testDtoRequests.getDeleteAlarmRequestDto());

        // Let's verify that it was saved in Dao:
        //
        verify(saasDaoMock).saveNewRequest(same(builder.getDeleteEntityRequest()), same(callbackMock));
    }

    @Test
    public void t3_whenPostedResyncAllStartSubdomainRequest_itIsSavedToDao() {

        // Let's publish a resync start request.
        saasPublisher.publishRequest(publishTaskMock, testDtoRequests.getResyncAllAlarmsStartRequestDto());

        // Let's verify that it was saved in Dao:
        //
        verify(saasDaoMock).saveNewRequest(same(builder.getResyncAllStartSubdomainRequest()), same(callbackMock));
    }

    @Test
    public void t4_whenPostedResyncAllEndSubdomainRequest_itIsSavedToDao() {

        // Let's publish a resync end request.
        saasPublisher.publishRequest(publishTaskMock, testDtoRequests.getResyncAllAlarmsEndRequestDto());

        // Let's verify that it was saved in Dao:
        //
        verify(saasDaoMock).saveNewRequest(same(builder.getResyncAllEndSubdomainRequest()), same(callbackMock));
    }

}