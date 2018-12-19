# K-Repository schemas (PoC)

The K-Repository schemas provides a logic to load Avro schemas (used on Kafka topics by K-Repository)
into designated Schema Registry.


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
mvn docker:build
```
