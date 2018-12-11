package com.j9soft.kbroker;

import kafka.admin.RackAwareMode;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.zk.AdminZkClient;
import kafka.zk.EmbeddedZookeeper;
import kafka.zk.KafkaZkClient;
import org.apache.kafka.common.utils.Time;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;


/**
 * Class running embedded Kafka Broker and Zookeeper.
 */
public class KBrokerApplication {

    private static final String ZKHOST = "127.0.0.1";
    private static final String BROKERHOST = "127.0.0.1";
    private static final String BROKERPORT = "9092";

    private static EmbeddedZookeeper zkServer;
    private static KafkaZkClient zkClient;
    private static KafkaServer kafkaServer;

    private static void init() throws IOException {

        // Start an embedded Kafka Server
        //
        // setup Zookeeper
        zkServer = new EmbeddedZookeeper();
        String zkConnect = ZKHOST + ":" + zkServer.port();  // @TODO we need a fixed zookeeper port
        //
        // setup Broker
        Properties brokerProps = new Properties();
        brokerProps.setProperty("zookeeper.connect", zkConnect);
        brokerProps.setProperty("broker.id", "0");
        brokerProps.setProperty("log.dirs", Files.createTempDirectory("kafka-").toAbsolutePath().toString());
        brokerProps.setProperty("listeners", "PLAINTEXT://" + BROKERHOST + ":" + BROKERPORT);
        brokerProps.setProperty("offsets.topic.replication.factor", "1");
        KafkaConfig config = new KafkaConfig(brokerProps);
        Time mock = new MockTime();
        kafkaServer = TestUtils.createServer(config, mock);
    }

    public static void main(String[] args) throws IOException {

        init();
        
        // @TODO wait for hook
        
        
    }
    
    private static void hook() {
        kafkaServer.shutdown();
        zkClient.close();

        // zkServer.shutdown();  @TODO uncomment when a patch is available for https://issues.apache.org/jira/browse/KAFKA-6291#
    }
}
