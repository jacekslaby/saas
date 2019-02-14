# K-Repository (PoC)

The K-Repository provides functionality of a datastore. (with Kafka as a backing database)


## Getting Started

```
# start the environment  (using the docker image created earlier)
docker-compose up -d

# stop the environment
docker-compose down
```

## Docker image

```
# first build and package an auxiliary tool
cd ../tool-kafka-topics
mvn package
cp target/kafka-topics-jar-with-dependencies.jar k-repository
# @TODO come with a better idea to do it ^^^^^   (maybe build image from the parent directory with option -f and in Dockerfile use directory names when COPY'ing. But it's not intuitive ?)  (perhaps put k-repository/Dockerfile in another directory, e.g. 'docker/k-repository/Dockerfile')

# build and put image in local docker
mvn package -Dmaven.test.skip=true
mvn docker:build
```

## Development

### Shortcuts

```
# stop previous run
./run-dev-docker-clean.sh 

# start zookeeper, kafka, schema-registry and register required schemas
./run-dev-docker-compose.sh 

# Setup the environment
set KR_BOOTSTRAP_SERVERS=kafka:9092
set KR_SCHEMA_REGISTRY_URL=http://schema-registry:8081
set KR_REPOSITORY_NAME=prodxphone-saas
# start K-Repository locally  (i.e. not in docker)
mvn compile exec:java
# alternatively start in docker
docker run -e KR_BOOTSTRAP_SERVERS=kafka:9092 -e "KR_SCHEMA_REGISTRY_URL=http://schema-registry:8081" -e KR_REPOSITORY_NAME=prodxphone-saas --name k-repository --rm --network=krepository_kafka_backend_net -it j9soft/k-repository:latest
```

### Start one by one

```
# (Note: if you want to clear data from the previous run then use: docker-compose -f docker-compose-dev.yml rm -fs
#   or manually remove all containers using docker-compose rm)

# start the environment (without K-Repository)           (Note: alternative is: docker-compose -f docker-compose-dev.yml up -d )
docker-compose up -d zookeeper
docker-compose up -d kafka

# wait 20 seconds for correct setup (kafka needs to be ready, because schema-registry creates a topic named _schemas)
sleep 20
docker-compose up -d schema-registry

#  (btw: in order to verify them you can use:
#    docker run --network=krepository_kafka_backend_net evpavel/kt kt topic -brokers kafka:9092
#    docker run --network=krepository_kafka_backend_net evpavel/kt kt topic -partitions -brokers kafka:9092
#    docker run --network=krepository_kafka_backend_net evpavel/kt kt consume -topic prodxphone-saas-v1-commands-topic -brokers kafka:9092
#    docker run --network=krepository_kafka_backend_net evpavel/kt kt consume -topic prodxphone-saas-v1-entities-topic -brokers kafka:9092
#   see also:
#     https://github.com/Paxa/kt
#  )

# wait 20 seconds for correct setup (schema-registry needs to become ready, because adding schemas writes them to SR and _schemas topic)
sleep 20

# (Note: Avro schemas registration is added to K-Repository container so it is no longer required to do it. BUT currently it registers junit Avro schemas, so for now we keep the below lines for reference.)

# Install Avro schemas.
#  (btw: in order to verify them you can use:
#    http://schema-registry:8081/subjects
#    http://schema-registry:8081/subjects/com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1/versions/1
#    http://schema-registry:8081/subjects/com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1/versions/latest
#   see also:
#     http://cloudurable.com/blog/kafka-avro-schema-registry/index.html
#     https://dzone.com/articles/kafka-avro-serialization-and-the-schema-registry
#  )
docker-compose run --rm  k-repository-schemas

# (Note: A health check logic is added to K-Repository container so it is no longer required to wait 10 seconds for correct setup (topics get created and schemas are registered))

# (assure that 'kafka' and 'schema-registry' point to your `docker-machine ip`, e.g. 192.168.99.100 on virtualbox
# e.g. by adding to hosts file)

# Setup the environment
set KR_BOOTSTRAP_SERVERS=kafka:9092
set KR_SCHEMA_REGISTRY_URL=http://schema-registry:8081
set KR_REPOSITORY_NAME=prodxphone-saas

# start K-Repository locally  (i.e. not in docker)
mvn compile exec:java
(or: mvn package & java -jar target/k-repository.jar)
```

### How to browse messages on topics

https://docs.confluent.io/current/streams/kafka-streams-examples/docs/index.html
https://docs.confluent.io/current/schema-registry/docs/serializer-formatter.html

```
# commands
docker-compose exec schema-registry     kafka-avro-console-consumer         --bootstrap-server kafka:9092         --topic prodxphone-saas-v1-commands-topic --from-beginning

docker-compose exec schema-registry     kafka-avro-console-consumer         --bootstrap-server kafka:9092         --topic prodxphone-saas-v1-commands-topic --from-beginning --property print.key=true   --property print.schema.ids=true   --property schema.id.separator=: --key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

# commands with partition id and offsets
docker run --name consume-commands --rm --network=krepository_kafka_backend_net evpavel/kt kt consume -topic prodxphone-saas-v1-commands-topic -brokers kafka:9092
docker rm consume-commands -f


# entities (btw: kafka-avro-console-consumer crashes on null message values when started with --property print.schema.ids=true)
docker-compose exec schema-registry     kafka-avro-console-consumer         --bootstrap-server kafka:9092         --topic prodxphone-saas-v1-entities-topic --from-beginning --property print.key=true --property print.timestamp=true  --key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

# entities with partition id and offsets
docker run --name consume-entities --rm --network=krepository_kafka_backend_net evpavel/kt kt consume -topic prodxphone-saas-v1-entities-topic -brokers kafka:9092
docker rm consume-entities -f
```
