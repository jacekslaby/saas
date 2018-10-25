package com.j9soft.saas.alarms;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import static junit.framework.TestCase.fail;

/*
 * I assume it is enough to test controller methods without testing HTTP wiring. (i.e. without TestRestTemplate, etc.)
 * (See also: https://spring.io/guides/gs/spring-boot/  @Autowired private TestRestTemplate template;  )
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasV1ControllerTest {

    private static final String DOMAIN = "controllerTest";
    private static final String ADAPTER_NAME = "controllerTestAdapter";

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

        fail("TODO");
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