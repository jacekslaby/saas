package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.model.Definitions;
import com.j9soft.saas.alarms.testdata.*;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.UUID;

import static junit.framework.TestCase.*;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

/*
 * I assume it is enough to test controller methods without testing HTTP wiring. (i.e. without TestRestTemplate, etc.)
 * (See also: https://spring.io/guides/gs/spring-boot/  @Autowired private TestRestTemplate template;  )
 *
 * The tests from this class verify whether expected operations are executed on SassDao instance
 * as a result of a HTTP request.
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

        fail("TODO");
    }

    @Test
    public void t4_whenPostedResyncRequests_theyAreSavedToDao() {

        TestResyncAllStartSubdomainRequest testStartRequest = TestResyncAllStartSubdomainRequest.build();
        TestResyncAllEndSubdomainRequest testEndRequest = TestResyncAllEndSubdomainRequest.build();

        // Let's POST list with both requests.
        saas.createRequestsWithList(testStartRequest.getDomain(), testStartRequest.getAdapterName(),
                "[" + testStartRequest.getRequestJson() +
                "," + testEndRequest.getRequestJson() + "]"
                );

        // Let's verify that both requests were saved in Dao:
        //
        // - some fields should be equal:
        // - other fields should be auto-generated

        ArgumentCaptor<SaasPublisher.Request[]> argument = ArgumentCaptor.forClass(SaasPublisher.Request[].class);
        verify(saasPublisherMock).publishRequestsWithArray(argument.capture());
        assertEquals("number of captured requests", argument.getValue().length, 2);

        checker.addResyncAllStartSubdomainRequest( testStartRequest.getRequestObject() );
        checker.addResyncAllEndSubdomainRequest( testEndRequest.getRequestObject() );

        // Let's verify that expected data were published:
        argument.getValue()[0].accept(checker);
        argument.getValue()[1].accept(checker);
    }

    private void postAndVerifyRequest(TestRequestData testRequestData) {

        // Let's POST a create entity request.
        saas.createRequest(testRequestData.getDomain(), testRequestData.getAdapterName(), testRequestData.getRequestJson());

        // Let's verify that it was published:
        //
        ArgumentCaptor<SaasPublisher.Request> argument = ArgumentCaptor.forClass(SaasPublisher.Request.class);
        verify(saasPublisherMock).publishRequest(argument.capture());
        //
        // Let's verify that expected data were published:
        argument.getValue().accept(checker);
    }

    private void verifyUuidAndEntryDate(CharSequence uuid, Long entryDateAsMillis) {
        assertTrue("proper uuid should be generated", UUID.fromString(uuid.toString()).version() > 0);
        assertThat(Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE, entryDateAsMillis, lessThanOrEqualTo(System.currentTimeMillis()));
    }

}