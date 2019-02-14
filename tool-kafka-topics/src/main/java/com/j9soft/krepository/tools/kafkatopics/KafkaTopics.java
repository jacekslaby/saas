package com.j9soft.krepository.tools.kafkatopics;

import kafka.zk.AdminZkClient;
import kafka.zk.KafkaZkClient;
import org.apache.kafka.common.utils.Time;
import scala.Option;

import java.util.Properties;

public class KafkaTopics {
    /**
     * Tool to check whether a topic exists. (If not it returns code 1.)
     * Example: java -cp target\classes com.j9soft.krepository.tools.kafkatopics.KafkaTopics --describe --zookeeper zookeeper:2181 --topic prodxphone-saas-v1-commands-topic
     */
    public static void main(String[] args) {

        if (args.length < 5
                || !"--describe".equals(args[0])
                || !"--zookeeper".equals(args[1])
                || !"--topic".equals(args[3])
                ) {
            // Incorrect syntax.
            System.err.println("Syntax error. Expected arguments (example): --describe --zookeeper zookeeper:2181 --topic prodxphone-saas-v1-commands-topic ");
            System.exit(1);
        }

        Boolean isSecure = false;
        int sessionTimeoutMs = 200000;
        int connectionTimeoutMs = 15000;
        int maxInFlightRequests = 10;
        Time time = Time.SYSTEM;
        String metricGroup = "myGroup";
        String metricType = "myType";
        String zkConnect = args[2];
        KafkaZkClient zkClient = KafkaZkClient.apply(zkConnect, isSecure, sessionTimeoutMs,
                connectionTimeoutMs, maxInFlightRequests, time, metricGroup, metricType);
        AdminZkClient adminZkClient = new AdminZkClient(zkClient);

        String topicName = args[4];

        // Based on:  https://www.analyticshut.com/streaming-services/kafka/create-and-list-kafka-topics-in-java/
        //            "Checking topic existence"
        //
        // (btw: i.e. we do not use deprecated code provided in:
        //   https://stackoverflow.com/questions/30572023/checking-the-existence-of-topic-in-kafka-before-creating-in-java )
        //   if (!AdminUtils.topicExists(zkClient, myTopic)){
        //     AdminUtils.createTopic(zkClient, myTopic, 2, 1, properties);
        //   }
        //
        if (zkClient.topicExists(topicName)) {
            // @TODO Not clear why the following always returns empty Properties... Bug in kafka libs ?
            Option<Properties> topicProperties = adminZkClient.getAllTopicConfigs().get(topicName);
            System.out.println(String.format("Topic properties: %s", topicProperties.get()));
        } else {
            // Topic does not exist.
            System.err.println(String.format("Topic does not exist: %s", topicName));
            System.exit(1);
        }

    }
}
