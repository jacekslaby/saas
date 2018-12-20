# K-Repository schemas (PoC)

The K-Repository IT provides a logic to execute Integration Tests (IT) of K-Repository.
Using docker-compose it sets up a Kafka (broker+zookeeper+schema registry + k-repository-schemas), k-repository
 and runs end-to-end scenarios defined in Cucumber.

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

# Start Up the required containers:
#
$ saas_run_on_local_docker__run_all_it.sh 



```

## Docker image

```
mvn docker:build
```
