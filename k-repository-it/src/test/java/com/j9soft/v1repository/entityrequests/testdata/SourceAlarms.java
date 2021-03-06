package com.j9soft.v1repository.entityrequests.testdata;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.EntityAttributes;
import com.j9soft.krepository.v1.entitiesmodel.Attributes;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

public enum SourceAlarms {
    A("A", "X", "A"),
    AinSubdomainX("AinSubdomainX", "X", "A"),
    AinSubdomainY("AinSubdomainY", "Y", "A"),
    B("B", "X", "B"),
    BinSubdomainX("BinSubdomainX", "X", "B"),
    CinSubdomainX("CinSubdomainX", "X", "C"),
    DinSubdomainY("DinSubdomainY", "Y", "D");

    SourceAlarms(String uniqueLabel, String subdomainName, String idInSubdomain) {
        // @TODO read SourceAlarm<uniqueLabel>.json (in order to have more attributes populated, with different values)
        this.uniqueLabel = uniqueLabel;
        this.subdomainName = subdomainName;
        this.idInSubdomain = idInSubdomain;
    }

    private static Map<String, SourceAlarms> labelsToSourceAlarms = new HashMap<>();
    static {
        for (SourceAlarms sourceAlarm : SourceAlarms.values()) {
            if (labelsToSourceAlarms.put(sourceAlarm.uniqueLabel, sourceAlarm) != null)
                throw new ExceptionInInitializerError("Label " + sourceAlarm.uniqueLabel + " for SourceAlarm " +
                        sourceAlarm + " has already been used");
        }
    }

    public static SourceAlarms forLabel(String uniqueLabel) {
        SourceAlarms result = labelsToSourceAlarms.get(uniqueLabel);
        assertNotNull(MessageFormat.format("error in test code, unsupported label:{0}", uniqueLabel), result);

        return result;
    }

    public static String SOURCE_ALARM = "SourceAlarm";
    private static String ENTITY_SUBDOMAIN_NAME = "Xphone:AdapterSiemens_nw";
    private static String ENTITY_ATTRIBUTE_NAME__NOTIFICATION_IDENTIFIER = "NotificationIdentifier";

    private String uniqueLabel;
    private String subdomainName;
    private String idInSubdomain;

    public CreateEntityRequestV1 buildCreateEntityRequest() {

        EntityAttributes entityAttributes = EntityAttributes.newBuilder()
                .setNotificationIdentifier(idInSubdomain)
                .setPerceivedSeverity("1")
                .build();

        return CreateEntityRequestV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(SOURCE_ALARM)
                .setEntitySubdomainName(subdomainName)
                .setEntityIdInSubdomain(idInSubdomain)
                .setEntityAttributes(entityAttributes)
                .build();
    }

    public EntityV1 buildEntity() throws IOException {

        Attributes attributes = Attributes.newBuilder()
                .setNotificationIdentifier(idInSubdomain)
                .setPerceivedSeverity("1")
                .build();

        // Note: Avro deserialization (of objects received from topic) is returning non-String objects (i.e. org.apache.avro.util.Utf8),
        //  so, in order to assure that equals() is working correctly in assertThat(),
        //  we need to build this entity object using Avro serialization/deserialization.
        return EntityV1.fromByteBuffer(
                EntityV1.newBuilder()
                    .setUuid(UUID.randomUUID().toString())
                    .setEntryDate(System.currentTimeMillis())
                    .setEntityTypeName(SOURCE_ALARM)
                    .setEntitySubdomainName(subdomainName)
                    .setEntityIdInSubdomain(idInSubdomain)
                    .setAttributes(attributes)
                    .build().toByteBuffer());
    }

    public static final String SCHEMA__UUID = "uuid";
    public static final String SCHEMA__ENTRY_DATE = "entry_date";
    public static final String SCHEMA__EVENT_DATE = "event_date";
    public static final String SCHEMA__ATTRIBUTES = "attributes";
}
