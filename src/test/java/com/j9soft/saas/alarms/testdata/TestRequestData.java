package com.j9soft.saas.alarms.testdata;

public abstract class TestRequestData {
    public static final String DOMAIN = "controllerTest";
    public static final String ADAPTER_NAME = "controllerTestAdapter";

    public String getDomain() { return DOMAIN; }

    public String getAdapterName() { return ADAPTER_NAME; }

    public abstract String getRequestJson();

}
