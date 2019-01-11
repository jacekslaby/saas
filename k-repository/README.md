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
# start the environment (without K-Repository)
docker-compose up -d zookeeper
docker-compose up -d kafka
docker-compose up -d schema-registry

# wait 10 seconds for correct setup (schema-registry needs to become ready)
sleep 10

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
