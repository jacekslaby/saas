
# see also: https://codefresh.io/howtos/using-docker-maven-maven-docker/   "Here is Dockerfile.testing"
#

FROM openjdk:11-jdk-slim

# Our Kafka broker port (port where it listens for requests)    (private only  - see http://crosbymichael.com/dockerfile-best-practices.html)
EXPOSE 9092

# Our Zookeeper port  (required by connections from Schema Registry)
# TODO: How to specify the port in the code ? Currently it is anonymous.
EXPOSE 9091

# Binaries of our app. (including dependencies)
COPY target/test-double-kafka-broker-1.0-SNAPSHOT-jar-with-dependencies.jar  /usr/src/test-double-kafka-broker-1.0-SNAPSHOT.jar

# Start the app.     (the array syntax - see http://crosbymichael.com/dockerfile-best-practices.html)
CMD ["java", "-jar", "/usr/src/test-double-kafka-broker-1.0-SNAPSHOT.jar"]

