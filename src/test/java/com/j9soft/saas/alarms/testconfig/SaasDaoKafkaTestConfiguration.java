package com.j9soft.saas.alarms.testconfig;


import com.j9soft.saas.alarms.dao.RequestDaoKafka;

public class SaasDaoKafkaTestConfiguration {

    private KafkaTestConnector client;

    public SaasDaoKafkaTestConfiguration() {
        client = new KafkaTestConnector();
        client.connect("127.0.0.1", 9092);
    }

    public RequestDaoKafka getDao() {
        return new RequestDaoKafka(client);
    }

    public String getTopicName() {
        return this.client.getTopicName();
    }

    public void close() {
        client.close();
    }
}
