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
# build and put in local docker
mvn docker:build
```

## Development

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

# wait 10 seconds for correct setup (topics get created and schemas are registered)
sleep 10

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
