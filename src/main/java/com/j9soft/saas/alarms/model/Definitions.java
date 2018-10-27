package com.j9soft.saas.alarms.model;

public class Definitions {
    public static final String ALARM_ENTITY_TYPE_NAME = "SourceAlarm";
    public static final String ALARM_ATTRIBUTE_NAME__EVENT_TIME = "event_time";
    public static final String ALARM_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER = "notification_identifier";
    public static final String ALARM_ATTRIBUTE_NAME__PERCEIVED_SEVERITY = "perceived_severity";

    public static final String DAO_SCHEMA_REQUEST__UUID = "uuid";
    public static final String DAO_SCHEMA_REQUEST__ENTRY_DATE = "entry_date";

    public static final String API_SCHEMA_ALARM__ALARM_DTO = "alarm_dto";
    public static final String API_SCHEMA_ALARM__ADDITIONAL_PROPERTIES = "additional_properties";
    public static final String API_SCHEMA_ALARM__EVENT_TIME = ALARM_ATTRIBUTE_NAME__EVENT_TIME;
    public static final String API_SCHEMA_ALARM__NOTIFICATION_IDENTIFIER = ALARM_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER;
    public static final String API_SCHEMA_ALARM__PERCEIVED_SEVERITY = ALARM_ATTRIBUTE_NAME__PERCEIVED_SEVERITY;
}
