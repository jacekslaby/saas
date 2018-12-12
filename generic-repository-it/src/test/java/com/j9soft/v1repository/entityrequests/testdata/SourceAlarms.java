package com.j9soft.v1repository.entityrequests.testdata;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

public enum SourceAlarms {
    A("A"),
    B("B");

    SourceAlarms(String uniqueLabel) {
        // @TODO read SourceAlarm<uniqueLabel>.json
        this.uniqueLabel = uniqueLabel;
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

    private static String SOURCE_ALARM = "SourceAlarm";

    private String uniqueLabel;

    public CreateEntityRequestV1 buildCreateEntityRequest() {
        return CreateEntityRequestV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(SOURCE_ALARM)
                .setEntitySubdomainName("@TODO entitySubdomainName")
                .setEntityIdInSubdomain("@TODO createAlarmRequest.getAlarmDto().getNotificationIdentifier()")
                .build();
    }

    // @TODO change to EntityV1
    public CreateEntityRequestV1 buildEntity() {
        return null;
    }
}
