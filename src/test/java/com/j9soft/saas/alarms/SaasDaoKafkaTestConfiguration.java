package com.j9soft.saas.alarms;


public class SaasDaoKafkaTestConfiguration {

    private KafkaTestConnector client;

    public SaasDaoKafkaTestConfiguration() {
        client = new KafkaTestConnector();
        client.connect("127.0.0.1", 9092);
    }

    public SaasDaoKafka getDao() {
        return new SaasDaoKafka(client);
    }

    public String getTopicName() {
        return this.client.getTopicName();
    }

    public void close() {
        client.close();
    }
}
