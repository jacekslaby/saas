package com.j9soft.saas.alarms.testdata;

import java.time.OffsetDateTime;

public class TestConstants {
    public static final String DOMAIN = "controllerTest";
    public static final String ADAPTER_NAME = "controllerTestAdapter";

    public static final String EVENT_TIME_STRING = "2018-10-19T13:44:56.334+02:00";
    public static final long EVENT_TIME = OffsetDateTime.parse(EVENT_TIME_STRING).toInstant().toEpochMilli();

}
