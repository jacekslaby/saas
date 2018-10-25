package com.j9soft.saas.alarms;


public class RaasDaoKafkaTestConfiguration {

    public static class AlarmDetails {
        String domain;
        String adapterName;
        String notificationIdentifier;
        String json;

        public AlarmDetails(String domain, String adapterName, String notificationIdentifier, String json) {
            this.domain = domain;
            this.adapterName = adapterName;
            this.notificationIdentifier = notificationIdentifier;
            this.json = json;
        }
    }

    public static final AlarmDetails EXISTING_ALARM =
            new AlarmDetails("ala", "ma", "kota", "{\"moIdentifier\":\"kot\"}");


    private KafkaConnector client;

    public RaasDaoKafkaTestConfiguration() {
        client = new KafkaConnector();
        client.connect("127.0.0.1", 9092);
    }

    public SaasDao getDao() {
        return new SaasDaoKafka(client);
    }

    public String getTopicName() {
        return this.client.getTopicName();
    }

    public void close() {
        client.close();
    }
}
