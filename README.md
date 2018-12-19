
* [Overview](#overview)
* [Running The Demos](#running-the-demos)
* [Prerequisities](#prerequisites)


# Overview

There are multiple demos in this repo that showcase proof of concept for SourceAlarms Repository (aka Source Active Alarms Store).
Each demo resides in its own subfolder.

| Demo                                       | Description 
| ------------------------------------------ | -------------------------------------------------------------------------------- 
| [k-repository](k-repository/README.md)     | K-Repository is a multi-type entities repository. It keeps data in Kafka topics.
| [k-repository-schemas](k-repository-schemas/README.md)     | K-Repository Schemas is a provisioning tool. It registeres required Avro schemas in Schema Registry.
| [k-repository-it](k-repository-it/README.md)     | Integration Tests (IT) of K-Repository. Using docker-compose it sets up a Kafka (broker+zookeeper+schema registry + k-repository-schemas), k-repository and runs end-to-end scenarios defined in Cucumber.
| [saas-repository](saas-repository/README.md)     | Saas-Repository itself. It is an instance of K-Repository. It uses SourceAlarm entity type.
| [saas-rest-proxy](saas-rest-proxy/README.md)     | The Saas REST Proxy provides a RESTful interface to Saas-Repository.


# Running The Demos

1. Clone the repo: `git clone https://github.com/jacekslaby/saas`
2. Change directory to one of the demo subfolders
3. Read the `README.md` for each demo for precise instructions

# Prerequisites

* Docker version 18.02.0+
* Docker Compose version 1.14.0 with Docker Compose file format 3.2
