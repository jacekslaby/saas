# K-Repository schemas (PoC)

The K-Repository IT provides a logic to execute Integration Tests (IT) of K-Repository.
Using docker-compose it sets up:
- Kafka (zookeeper + broker + schema registry)
- required Avro schemas (k-repository-schemas) in schema registry
- and a tested instance of K-Repository.

Afterwards it runs end-to-end scenarios defined in Cucumber.

## Getting Started

```
# Build images of modules: (and put them in the local docker)
#  k-repository          - repository to be tested
#  k-repository-it       - integration tests to be executed
#  k-repository-schemas  - scripts to create Avro schemas required by k-repository.
#
$ saas_run_on_local_docker__install_all_images.sh 

$ docker image ls
REPOSITORY                         TAG                  IMAGE ID            CREATED             SIZE
j9soft/k-repository-it             latest               7556d2297cd1        10 seconds ago      119MB
j9soft/k-repository                latest               79d1cee038e6        26 seconds ago      129MB
j9soft/k-repository-schemas        latest               0a221441d479        21 hours ago        119MB

# Start Up the required containers and run ITs.
#
$ saas_run_on_local_docker__run_all_it.sh 
```

## Building Docker image

```
mvn docker:build
```

## Running locally (not from Docker) during development

```
set GRIT_BOOTSTRAP_SERVERS=kafka:9092
set GRIT_SCHEMA_REGISTRY_URL="http://schema-registry:8081"
set GRIT_REPOSITORY_NAME=prodxphone-saas

mvn verify
```
