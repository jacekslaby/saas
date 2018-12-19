# K-Repository (PoC)

The K-Repository provides functionality of a store. (with Kafka as a database)


## Getting Started

```
k-repository-schemas> mvn schema-registry:register
[INFO] Scanning for projects...
[INFO]
[INFO] ------------< com.j9soft.saas.alarms:k-repository-schemas >-------------
[INFO] Building k-repository-schemas 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- kafka-schema-registry-maven-plugin:5.1.0:register (default-cli) @ k-repository-schemas ---
[INFO] Registered subject(com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1) with id 1 version 1
[INFO] Registered subject(com.j9soft.krepository.v1.entitiesmodel.EntityV1) with id 3 version 1
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.520 s
[INFO] Finished at: 2018-12-19T15:15:19Z
[INFO] ------------------------------------------------------------------------

k-repository-schemas> mvn schema-registry:test-compatibility
[INFO] Scanning for projects...
[INFO]
[INFO] ------------< com.j9soft.saas.alarms:k-repository-schemas >-------------
[INFO] Building k-repository-schemas 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- kafka-schema-registry-maven-plugin:5.1.0:test-compatibility (default-cli) @ k-repository-schemas ---
[INFO] Schema p:\Projekty\saas\k-repository-schemas\src\main\avro\create-entity-request-v1.avsc is compatible with subject(com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1)
[INFO] Schema p:\Projekty\saas\k-repository-schemas\src\main\avro\entity-v1.avsc is compatible with subject(com.j9soft.krepository.v1.entitiesmodel.EntityV1)
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.545 s
[INFO] Finished at: 2018-12-19T15:16:26Z
[INFO] ------------------------------------------------------------------------

```

## Docker image

```
# build and put in local docker
mvn docker:build
```

## Runtime

```
# start the environment  (using the docker image created earlier)
docker-compose up -d

# stop the environment
docker-compose down
```

## Development

```
# start the environment (without K-Repository)
docker-compose up -d zookeeper
docker-compose up -d kafka
docker-compose up -d schema-registry
docker-compose run --rm  k-repository-schemas

# wait 10 seconds for correct setup (topics get created and schemas are registered)

# start K-Repository locally  (i.e. not in docker)
mvn compile exec:java

```
