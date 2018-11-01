package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.testdata.*;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.verify;

/*
 * I assume it is enough to test controller methods without testing HTTP wiring. (i.e. without TestRestTemplate, etc.)
 * (See also: https://spring.io/guides/gs/spring-boot/  @Autowired private TestRestTemplate template;  )
 *
 * The tests from this class verify whether as a result of a HTTP request
 * the expected input parameters are delivered to a SassDao instance.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasV1ControllerTest {

    private SaasV1 saas;
    private SaasPublisher saasPublisherMock;
    private CapturedRequestChecker checker;

    @Before
    public void initRaas() {

        // Let's create a mock which will be checked for expected operation calls.
        //
        saasPublisherMock = Mockito.mock(SaasPublisher.class);

        // Let's create the tested bean.
        saas = new SaasV1Controller(new SaasV1Service(this.saasPublisherMock));

        // Let's create test checker. (i.e. a visitor which will verify that arguments passed to the mock are correct)
        this.checker = CapturedRequestChecker.newBuilder();
    }

    @Test
    public void t1_whenPostedCreateAlarmRequest_itIsSavedToDao() {

        TestCreateEntityRequest testRequest = TestCreateEntityRequest.build();
        checker.addCreateEntityRequest( testRequest.getRequestObject() );

        postAndVerifyRequest(testRequest);
    }

    @Test
    public void t2_whenPostedDeleteAlarmRequest_itIsSavedToDao() {

        TestDeleteEntityRequest testRequest = TestDeleteEntityRequest.build();
        checker.addDeleteEntityRequest( testRequest.getRequestObject() );

        postAndVerifyRequest(testRequest);
    }

    @Test
    public void t3_whenPostedBunchOfRequests_theyAreSavedToDao() {

        // Our test bunch of requests is as follows:
        //   DeleteAlarmRequest, ResyncAllStartSubdomainRequest, CreateAlarmRequest and finally ResyncAllEndSubdomainRequest.
        //
        TestDeleteEntityRequest testDeleteRequest = TestDeleteEntityRequest.build();
        checker.addDeleteEntityRequest( testDeleteRequest.getRequestObject() );
        TestResyncAllStartSubdomainRequest testStartRequest = TestResyncAllStartSubdomainRequest.build();
        checker.addResyncAllStartSubdomainRequest( testStartRequest.getRequestObject() );
        TestCreateEntityRequest testCreateRequest = TestCreateEntityRequest.build();
        checker.addCreateEntityRequest( testCreateRequest.getRequestObject() );
        TestResyncAllEndSubdomainRequest testEndRequest = TestResyncAllEndSubdomainRequest.build();
        checker.addResyncAllEndSubdomainRequest( testEndRequest.getRequestObject() );

        postAndVerifyRequestsWithArray(new TestRequestData[] {
                testDeleteRequest,
                testStartRequest,
                testCreateRequest,
                testEndRequest} );
    }

    @Test
    public void t4_whenPostedResyncRequests_theyAreSavedToDao() {

        // Resync is when two requests come: ResyncAllStartSubdomainRequest followed by ResyncAllEndSubdomainRequest.
        //
        TestResyncAllStartSubdomainRequest testStartRequest = TestResyncAllStartSubdomainRequest.build();
        checker.addResyncAllStartSubdomainRequest( testStartRequest.getRequestObject() );
        TestResyncAllEndSubdomainRequest testEndRequest = TestResyncAllEndSubdomainRequest.build();
        checker.addResyncAllEndSubdomainRequest( testEndRequest.getRequestObject() );

        postAndVerifyRequestsWithArray(new TestRequestData[] {
                testStartRequest,
                testEndRequest} );
    }

    private void postAndVerifyRequestsWithArray(TestRequestData[] testRequestDataArray) {

        TestRequestData firstTestRequest = testRequestDataArray[0];

        // Let's concatenate JSON from all requests.
        StringBuilder requestJson = new StringBuilder();
        requestJson.append('[');
        requestJson.append(firstTestRequest.getRequestJson());
        for (int i = 1; i < testRequestDataArray.length; i++) {
            requestJson.append(',');
            requestJson.append(testRequestDataArray[i].getRequestJson());
        }
        requestJson.append(']');

        // Let's POST list with all requests.
        saas.createRequestsWithList(firstTestRequest.getDomain(), firstTestRequest.getAdapterName(),
                requestJson.toString() );

        // Let's verify that requests were saved in Dao:
        ArgumentCaptor<SaasPublisher.Request[]> argument = ArgumentCaptor.forClass(SaasPublisher.Request[].class);
        verify(saasPublisherMock).publishRequestsWithArray(argument.capture());
        SaasPublisher.Request[] capturedRequests = argument.getValue();
        assertEquals("number of captured requests", capturedRequests.length, testRequestDataArray.length);

        // Let's verify that expected data were delivered to SaasDao:
        for (int i = 0; i < testRequestDataArray.length; i++) {
            capturedRequests[i].accept(checker);
        }
    }

    private void postAndVerifyRequest(TestRequestData testRequestData) {

        // Let's POST a create entity request.
        saas.createRequest(testRequestData.getDomain(), testRequestData.getAdapterName(), testRequestData.getRequestJson());

        // Let's verify that it was published:
        //
        ArgumentCaptor<SaasPublisher.Request> argument = ArgumentCaptor.forClass(SaasPublisher.Request.class);
        verify(saasPublisherMock).publishRequest(argument.capture());
        //
        // Let's verify that expected data were delivered to SaasDao:
        argument.getValue().accept(checker);
    }

}