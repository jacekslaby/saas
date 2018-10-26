package com.j9soft.saas.alarms;

import com.j9soft.saas.alarms.model.CreateEntityRequest;
import com.j9soft.saas.alarms.model.Definitions;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

/*
 * I assume it is enough to test controller methods without testing HTTP wiring. (i.e. without TestRestTemplate, etc.)
 * (See also: https://spring.io/guides/gs/spring-boot/  @Autowired private TestRestTemplate template;  )
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasV1ControllerTest {

    private static final String DOMAIN = "controllerTest";
    private static final String ADAPTER_NAME = "controllerTestAdapter";

    private static final String ALARM_NOID = "eric2g:341";
    private static final String ALARM_JSON = "{\"severity\"=\"1\"}";
    private static final String REQUEST_JSON = "{\"@TODO\"=\"1\"}";

    private SaasV1 saas;
    private SaasDao saasDaoMock;

    @Before
    public void initRaas() {

        // Let's register what should be returned.
        //
        saasDaoMock = Mockito.mock(SaasDao.class);

        // Let's create the tested bean.
        saas = new SaasV1Controller(this.saasDaoMock);
    }

    @Test
    public void whenPostedCreateAlarmRequest_itIsSavedToDao() {

        // Let's post a create request.
        // @TODO prepare a proper JSON here
        saas.createRequest(DOMAIN, ADAPTER_NAME, REQUEST_JSON);

        // Expected request to be generated.
        Map<CharSequence,CharSequence> alarmDTO = new HashMap<>();
        alarmDTO.put("notification_identifier", ALARM_NOID);
        alarmDTO.put("perceived_severity", "1");
        CreateEntityRequest request = CreateEntityRequest.newBuilder()
                .setUuid("foo")  // we expect it to be overwritten
                .setEntryDate(1)  // we expect it to be overwritten
                .setEntityTypeName(Definitions.ALARM_ENTITY_TYPE_NAME)
                .setEntityDomainName(DOMAIN)
                .setEntitySubdomainName(ADAPTER_NAME)
                .setEntityIdInSubdomain(ALARM_NOID)
                .setEntityAttributes(alarmDTO)
                .setEventDate(System.currentTimeMillis())
                .setLineageStartDate(Long.valueOf(1))  // we expect it to be overwritten
                .build();

        // Let's verify that it was saved in Dao.
        // @TODO change lambda, because we cannot check equals() on every field
        verify(saasDaoMock).createRequest(argThat(arg -> arg.equals(request)));
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