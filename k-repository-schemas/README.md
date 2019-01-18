# K-Repository Schemas (PoC)

The K-Repository Schemas provides a logic to load Avro schemas (used on Kafka topics by K-Repository)
into designated Schema Registry.


## Getting Started

```
# (assure that 'schema-registry' points to your `docker-machine ip`, e.g. 192.168.99.100 on virtualbox
# e.g. by adding to hosts file)

# Setup the environment
set KRS_SCHEMA_REGISTRY_URL="http://schema-registry:8081"

mvn schema-registry:register
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

mvn schema-registry:test-compatibility
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

## Browsing schemas

http://schema-registry:8081/subjects
http://schema-registry:8081/subjects/com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1/versions
http://schema-registry:8081/subjects/com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1/versions/latest
http://schema-registry:8081/subjects/com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1/versions/2

## Docker image

```
mvn docker:build
```
