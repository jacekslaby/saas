# We need to work around openjdk:8u151-jre-alpine lacking some libs.
# ( https://stackoverflow.com/questions/51368650/kafka-streams-allmetadata-returns-empty-list-in-docker-interactive-queries            Using this approach, i.e. anapsix/alpine-java
#   https://issues.apache.org/jira/browse/KAFKA-4988
#   https://stackoverflow.com/questions/50288034/unsatisfiedlinkerror-tmp-snappy-1-1-4-libsnappyjava-so-error-loading-shared-li        Did not work.
# )
FROM anapsix/alpine-java:8u192b12_server-jre

# Files required to register initial Avro schemas.
RUN apk add jq
RUN apk add curl
COPY src/main/avro /k-repository-schemas/

# Binaries of our app. (including dependencies)
# (We do not want any version in the name of jar file. See https://stackoverflow.com/questions/50896123/what-is-the-best-practice-to-get-the-file-name-that-was-defined-in-the-pom-xml )
#
COPY target/k-repository.jar  /app/k-repository.jar

# Script to start k-repository. 
# - waits for schema-registry to be available and registers minimal Avro schemas
#
COPY start-k-repository.sh /app/start-k-repository.sh
RUN chmod a+x /app/start-k-repository.sh

# Tool to check for topics.
COPY kafka-topics-jar-with-dependencies.jar  /tools/kafka-topics-jar-with-dependencies.jar

WORKDIR /app

# Start the app.     (the array syntax - see http://crosbymichael.com/dockerfile-best-practices.html)
CMD ["/app/start-k-repository.sh"]

# Note:
#
# The app expects the following resources to be provided:
#
# - environment variables:   (given here with example values)
#     KR_BOOTSTRAP_SERVERS: kafka:9092
#     KR_SCHEMA_REGISTRY_URL: "http://schema-registry:8081"
#     KR_REPOSITORY_NAME: prodxphone-saas
#
# - network connectivity to the above. 