package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.testdata.TestCreateEntityRequest;
import com.j9soft.saas.alarms.testdata.TestDeleteEntityRequest;
import com.j9soft.saas.alarms.testdata.TestResyncAllEndSubdomainRequest;
import com.j9soft.saas.alarms.testdata.TestResyncAllStartSubdomainRequest;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

/*
 * The tests from this class verify
 * whether expected operations are executed on SaasDaoKafka instance
 * as a result of a request to SaasPublisherKafka.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasPublisherKafkaTest {

    private SaasPublisher saasPublisher;
    private SaasDaoKafka saasDaoMock;

    @Before
    public void initRaas() {

        // Let's create a Dao mock which will be checked for expected operation calls.
        //
        saasDaoMock = Mockito.mock(SaasDaoKafka.class);

        // Let's create the tested bean.
        saasPublisher = new SaasPublisherKafka(this.saasDaoMock);
    }

    @Test
    public void t1_whenPostedCreateAlarmRequest_itIsSavedToDao() {

        TestCreateEntityRequest testRequest = TestCreateEntityRequest.build();
        SaasPublisher.Request request = SaasPublisher.CreateEntityRequest.newBuilder()
                .setWrappedRequest(testRequest.getRequestObject());

        // Let's publish a create entity request.
        saasPublisher.publishRequest(request);

        // Let's verify that it was saved in Dao:
        //
        verify(saasDaoMock).createRequest(same(testRequest.getRequestObject()));
    }

    @Test
    public void t2_whenPostedDeleteAlarmRequest_itIsSavedToDao() {

        TestDeleteEntityRequest testRequest = TestDeleteEntityRequest.build();
        SaasPublisher.Request request = SaasPublisher.DeleteEntityRequest.newBuilder()
                .setWrappedRequest(testRequest.getRequestObject());

        // Let's publish a delete entity request.
        saasPublisher.publishRequest(request);

        // Let's verify that it was saved in Dao:
        //
        verify(saasDaoMock).createRequest(same(testRequest.getRequestObject()));
    }

    @Test
    public void t3_whenPostedResyncAllStartSubdomainRequest_itIsSavedToDao() {

        TestResyncAllStartSubdomainRequest testRequest = TestResyncAllStartSubdomainRequest.build();
        SaasPublisher.Request request = SaasPublisher.ResyncAllStartSubdomainRequest.newBuilder()
                .setWrappedRequest(testRequest.getRequestObject());

        // Let's publish a delete entity request.
        saasPublisher.publishRequest(request);

        // Let's verify that it was saved in Dao:
        //
        verify(saasDaoMock).createRequest(same(testRequest.getRequestObject()));
    }

    @Test
    public void t4_whenPostedResyncAllEndSubdomainRequest_itIsSavedToDao() {

        TestResyncAllEndSubdomainRequest testRequest = TestResyncAllEndSubdomainRequest.build();
        SaasPublisher.Request request = SaasPublisher.ResyncAllEndSubdomainRequest.newBuilder()
                .setWrappedRequest(testRequest.getRequestObject());

        // Let's publish a delete entity request.
        saasPublisher.publishRequest(request);

        // Let's verify that it was saved in Dao:
        //
        verify(saasDaoMock).createRequest(same(testRequest.getRequestObject()));
    }

}