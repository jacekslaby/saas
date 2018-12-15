FROM openjdk:8-jre-alpine

WORKDIR /app

# Binaries of our app. (including dependencies)
# (We do not want any version in the name of jar file. See https://stackoverflow.com/questions/50896123/what-is-the-best-practice-to-get-the-file-name-that-was-defined-in-the-pom-xml )
#
COPY target/k-repository-jar-with-dependencies.jar  /app/k-repository.jar

# Start the app.     (the array syntax - see http://crosbymichael.com/dockerfile-best-practices.html)
CMD ["java", "-jar", "/app/k-repository.jar"]


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