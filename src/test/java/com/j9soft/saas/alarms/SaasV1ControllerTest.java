package com.j9soft.saas.alarms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.j9soft.saas.alarms.model.CreateEntityRequest;
import com.j9soft.saas.alarms.model.Definitions;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;

/*
 * I assume it is enough to test controller methods without testing HTTP wiring. (i.e. without TestRestTemplate, etc.)
 * (See also: https://spring.io/guides/gs/spring-boot/  @Autowired private TestRestTemplate template;  )
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasV1ControllerTest {

    private SaasV1 saas;
    private SaasDao saasDaoMock;

    @Before
    public void initRaas() {

        // Let's register what should be returned.
        //
        saasDaoMock = Mockito.mock(SaasDao.class);

        // Let's create the tested bean.
        saas = new SaasV1Controller(this.saasDaoMock, new SaasV1Service()); // @TODO think about encapsulated autowire for SaasV1Service
    }

    @Test
    public void whenPostedCreateAlarmRequest_itIsSavedToDao() throws JsonProcessingException {

        TestCreateEntityRequest testCreateEntityRequest = new TestCreateEntityRequest().build();

        // Let's post a create request.
        saas.createRequest(testCreateEntityRequest.getDomain(), testCreateEntityRequest.getAdapterName(),
                testCreateEntityRequest.getRequestJson());

        // Let's verify that it was saved in Dao:
        //
        // - some fields should be equal:
        verify(saasDaoMock).createRequest(
                refEq(testCreateEntityRequest.getRequestObject(),
                        Definitions.DAO_SCHEMA_REQUEST__UUID, Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE)); // fields excluded from comparison
        //
        // - other fields should be auto-generated
        ArgumentCaptor<CreateEntityRequest> argument = ArgumentCaptor.forClass(CreateEntityRequest.class);
        verify(saasDaoMock).createRequest(argument.capture());
        assertTrue("proper uuid should be generated", UUID.fromString(argument.getValue().getUuid().toString()).version() > 0);
        assertThat(Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE,
                argument.getValue().getEntryDate(), lessThanOrEqualTo(System.currentTimeMillis()));
    }

    @Test
    public void whenPostedDeleteAlarmRequest_itIsSavedToDao() {

        fail("TODO");
    }

    @Test
    public void whenPostedBunchOfRequests_theyAreSavedToDao() {

        fail("TODO");
    }

    @Test
    public void whenPostedResyncRequests_theyAreSavedToDao() {

        fail("TODO");
    }

}