package com.j9soft.saas.alarms;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.argThat;
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
    public void t1_whenPostedCreateAlarmRequest_itIsSavedToDao() throws JsonProcessingException {

        TestCreateEntityRequest testCreateEntityRequest = new TestCreateEntityRequest().build();
        SaasPublisher.Request request = SaasPublisher.CreateEntityRequest.newBuilder()
                .setWrappedRequest(testCreateEntityRequest.getRequestObject());

        // Let's publish a create entity request.
        saasPublisher.createRequest(request);

        // Let's verify that it was saved in Dao:
        //
        verify(saasDaoMock).createRequest(same( testCreateEntityRequest.getRequestObject() ));
    }
/*
    @Test
    public void t2_whenPostedDeleteAlarmRequest_itIsSavedToDao() {

    @Test
    public void t3_whenPostedBunchOfRequests_theyAreSavedToDao() {

        fail("TODO");
    }

    @Test
    public void t4_whenPostedResyncRequests_theyAreSavedToDao() {

    }

*/
}