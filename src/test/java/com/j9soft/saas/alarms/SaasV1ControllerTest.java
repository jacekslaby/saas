package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.controller.SaasV1Controller;
import com.j9soft.saas.alarms.model.RequestDto;
import com.j9soft.saas.alarms.model.RequestsListDto;
import com.j9soft.saas.alarms.service.PublishTask;
import com.j9soft.saas.alarms.service.SaasPublisher;
import com.j9soft.saas.alarms.service.SaasV1Service;
import com.j9soft.saas.alarms.testdata.CapturedRequestChecker;
import com.j9soft.saas.alarms.testdata.TestConstants;
import com.j9soft.saas.alarms.testdata.TestDaoRequestsBuilder;
import com.j9soft.saas.alarms.testdata.TestDtoRequests;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private NotWaitingPublishTask publishTask;
    private CapturedRequestChecker checker;
    private TestDaoRequestsBuilder builder;
    private TestDtoRequests testDtoRequests;

    class NotWaitingPublishTask extends PublishTask {
        private Exception[] exceptions;

        void setResults(Exception[] exceptions) {
            this.exceptions = exceptions;
        }

        public void onCompletion(int messageNum, Exception exception) {

        }
        public Exception[] getResults() throws InterruptedException {
            return exceptions;
        }
    }

    @Before
    public void initRaas() {

        // Let's create a mock which will be checked for expected operation calls.
        //
        publishTask = new NotWaitingPublishTask();
        saasPublisherMock = Mockito.mock(SaasPublisher.class);
        when(saasPublisherMock.createNewTask()).thenReturn(publishTask);

        // Let's create the tested bean.
        saas = new SaasV1Controller(new SaasV1Service(this.saasPublisherMock));

        // Let's create test checker. (i.e. a visitor which will verify that arguments passed to the mock are correct)
        this.checker = CapturedRequestChecker.newBuilder();

        builder = TestDaoRequestsBuilder.newBuilder();
        testDtoRequests = TestDtoRequests.newBuilder(builder);

        checker.addCreateEntityRequest( builder.getCreateEntityRequest() );
        checker.addDeleteEntityRequest( builder.getDeleteEntityRequest() );
        checker.addResyncAllEndSubdomainRequest( builder.getResyncAllEndSubdomainRequest() );
        checker.addResyncAllStartSubdomainRequest( builder.getResyncAllStartSubdomainRequest() );
    }

    @Test
    public void t1_whenPostedCreateAlarmRequest_itIsSavedToDao() {

        postAndVerifyRequest( testDtoRequests.getCreateAlarmDto() );
    }

    @Test
    public void t2_whenPostedDeleteAlarmRequest_itIsSavedToDao() {

        postAndVerifyRequest( testDtoRequests.getDeleteAlarmDto() );
    }

    @Test
    public void t3_whenPostedBunchOfRequests_theyAreSavedToDao() {

        // Our test bunch of requests is as follows:
        //   DeleteAlarmRequest, ResyncAllStartSubdomainRequest, CreateAlarmRequest and finally ResyncAllEndSubdomainRequest.

        postAndVerifyRequestsWithArray(new RequestDto[] {
                testDtoRequests.getDeleteAlarmDto(),
                testDtoRequests.getResyncAllStartDto(),
                testDtoRequests.getCreateAlarmDto(),
                testDtoRequests.getResyncAllEndDto()} );
    }

    @Test
    public void t4_whenPostedResyncRequests_theyAreSavedToDao() {

        // Resync is when two requests come: ResyncAllStartSubdomainRequest followed by ResyncAllEndSubdomainRequest.
        //

        postAndVerifyRequestsWithArray(new RequestDto[] {
                testDtoRequests.getResyncAllStartDto(),
                testDtoRequests.getResyncAllEndDto()} );
    }

    private void postAndVerifyRequestsWithArray(RequestDto[] requestDtoArray) {

        // Let's stub results to be received by SaasV1Service.
        publishTask.setResults(new Exception[requestDtoArray.length]);

        RequestsListDto requestsListDto = new RequestsListDto();
        requestsListDto.setRequests(requestDtoArray);

        // Let's POST list with all requests.
        saas.createRequestsWithList(TestConstants.DOMAIN, TestConstants.ADAPTER_NAME, requestsListDto);

        // Let's verify that requests were saved in Dao:
        ArgumentCaptor<RequestDto[]> argument = ArgumentCaptor.forClass(RequestDto[].class);
        verify(saasPublisherMock).publishRequestsWithArray(any(), argument.capture());
        RequestDto[] capturedRequests = argument.getValue();
        assertEquals("number of captured requests", capturedRequests.length, requestDtoArray.length);

        // Let's verify that expected data were delivered to SaasDao:
        for (int i = 0; i < requestDtoArray.length; i++) {
            capturedRequests[i].saveInDao(checker, null);
        }
    }

    private void postAndVerifyRequest(RequestDto requestDto) {

        // Let's stub results to be received by SaasV1Service.
        publishTask.setResults(new Exception[] {null});

        // Let's POST a create entity request.
        saas.createRequest(TestConstants.DOMAIN, TestConstants.ADAPTER_NAME, requestDto);

        // Let's verify that it was published:
        //
        ArgumentCaptor<RequestDto> argument = ArgumentCaptor.forClass(RequestDto.class);
        verify(saasPublisherMock).publishRequest(any(), argument.capture());
        //
        // Let's verify that expected data were delivered to SaasDao:
        argument.getValue().saveInDao(checker, null);
    }

}