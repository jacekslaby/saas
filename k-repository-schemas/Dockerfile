FROM maven:3.5.3-jdk-8-alpine 

#COPY wait-for-it.sh /usr/bin
#RUN chmod +x /usr/bin/wait-for-it.sh

# Prepare Avro schema files in the image
#
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/

# Register our schemas using kafka-schema-registry-maven-plugin. (`mvn schema-registry:register`) (`mvn schema-registry:test-compatibility`)
#
CMD ["mvn", "schema-registry:register"]

# Note: In order to speedup a build you may provide a re-used volume where maven will store downloaded dependencies, e.g.:
# docker run --mount source=maven_repository,target=/root/.m2  j9soft/generic-repository-it:latest
#
# See also: https://github.com/carlossg/docker-maven

# See also:
#   https://docs.confluent.io/current/schema-registry/docs/maven-plugin.html#sr-maven-plugin
#   https://github.com/confluentinc/examples/blob/5.0.0-post/clients/avro/pom.xml
#   https://docs.confluent.io/current/schema-registry/docs/schema_registry_tutorial.html