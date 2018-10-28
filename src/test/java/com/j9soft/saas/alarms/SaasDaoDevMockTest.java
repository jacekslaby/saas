package com.j9soft.saas.alarms;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaasDaoDevMockTest {

    private static SaasDaoTestScenarios scenarios;

    @BeforeClass
    public static void initDao() {
        scenarios = new SaasDaoTestScenarios( new SaasDaoDevMock() );
    }

    @Test
    public void t1_todo() {
        scenarios.t1_todo();
    }

}
