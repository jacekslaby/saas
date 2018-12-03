# Saas REST proxy (PoC)

Saas REST proxy provides you the Command API ('C' from 'CQRS') for Source Active Alarms Store. 
The API is described in [OpenAPI spec](/jacekslaby/saas/src/doc/openapi.yaml) format.

## Getting Started

```
git clone https://github.com/jacekslaby/saas.git
cd saas
mvn clean test
```

## Browsing API specification

Open [Swagger Editor](https://editor.swagger.io/) and paste contents of [OpenAPI spec](/jacekslaby/saas/src/doc/openapi.yaml).

## Test run without connecting to Kafka broker 
(i.e. with commands directed to nowhere)
```
mvn exec:java            (or: mvn spring-boot:run)

curl -X POST http://localhost:8080/v1/domains/Xphone/adapters/Eric2g/request -H "Content-type: application/json" -d \
'{
  "request_type": "CreateAlarmRequest",
  "request_content": {
    "alarm_dto": {
      "notification_identifier": "eric2g:south:34566",
      "event_time": "2018-10-19T13:44:56.334+02:00",
      "perceived_severity": 1
    }
  }
}'
```

## Test run with connecting to Kafka broker
(Note: The example below assumes we have broker at 192.168.33.10:9092)
1. First you need to start Kafka    (e.g. in a vagrant box)
```
Vagrantfile needs to contain:  config.vm.network "private_network", ip: "192.168.33.10"
vagrant up
vagrant ssh
cd kafka_2.11-1.1.0/config
perl -pi -e 's/#advertised.listeners.*/advertised.listeners=PLAINTEXT:\/\/192.168.33.10:9092/g'  server.properties
cd ..
bin/kafka-server-start.sh config/server.properties &
```

2. Then you may start the service and send some requests.
```
mvn spring-boot:run -Dspring-boot.run.profiles=kafka-dev -Dspring-boot.run.jvmArguments="-Dkafka-host=192.168.33.10 -Dkafka-port=9092"
(alternative: java -jar -Dspring.profiles.active=kafka-dev -Dkafka-host=192.168.33.10 -Dkafka-port=9092   target\saas-1.0-SNAPSHOT.jar)

curl -X POST http://localhost:8080/v1/domains/Xphone/adapters/Eric2g/request -H "Content-type: application/json" -d \
'{
  "request_type": "CreateAlarmRequest",
  "request_content": {
    "alarm_dto": {
      "notification_identifier": "eric2g:south:34566",
      "event_time": "2018-10-19T13:44:56.334+02:00",
      "perceived_severity": 1
    }
  }
}'
```

3. It is possible to observe messages
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic v1-commands-topic --from-beginning
```

# Implementation notes

## Avro Schemas and OpenAPI Schemas

Java classes generated from Avro and OpenAPI schemas are not kept in git.
They are automatically generated during `mvn compile`.

## Avro Schema evolution rules

Avro schemas are used to serialize values sent to Kafka topic. (e.g. schemas like `CreateEntityRequestV1`, `ResyncAllStartSubdomainRequestV1`, etc.)

The first version of any schema is "tagged" with suffix 'V1' in schema name. 
**Every** change made to a schema must be FULLy compatible (as understood in this [article](http://cloudurable.com/blog/kafka-avro-schema-registry/index.html)),
i.e. changes are forwards and backwards compatible from the latest to new and from new to the latest.

Rules (copied from [here](http://cloudurable.com/blog/kafka-avro-schema-registry/index.html)):
- You can add a field with a default to a schema. 
- You can remove a field that had a default value. 
- You can change a field's order attribute. 
- You can change a field's default value to another value or add a default value to a field that did not have one. 
- You can remove or add a field alias (keep in mind that this could break some consumers that depend on the alias). 
- You can change a type to a union that contains original type. 
(Note: Adding a new value to an enum is NOT forward compatible.)

See also:
- section "recommendations specific to Avro" from [Why Avro For Kafka Data?](https://www.confluent.io/blog/avro-kafka-data/)
- [Schema evolution in Avro, Protocol Buffers and Thrift](https://martin.kleppmann.com/2012/12/05/schema-evolution-in-avro-protocol-buffers-thrift.html)

Note: If we need to introduce an incompatible change then we must create a new schema (e.g. `CreateEntityRequestV2`) and provide a translator logic (e.g. docker image) able to continuously receive one version and publish the other. (e.g. If we intend to move all clients to `V2` then we need to provide a container which receives `CreateEntityRequestV1` objects, transforms them and sends as `CreateEntityRequestV2`.)
This approach allows us to migrate all components without a downtime. (We can decomission support for `V1`, including the translator logic, when it is no longer used by any component.)