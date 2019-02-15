
# see also: https://codefresh.io/howtos/using-docker-maven-maven-docker/   "Here is Dockerfile.testing"
#

FROM maven:3.5.3-jdk-8-alpine 

#COPY wait-for-it.sh /usr/bin
#RUN chmod +x /usr/bin/wait-for-it.sh

# Prepare Integration Test scenarios in the image
#
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/

# Let's fetch all maven dependencies including plugin dependencies.
#
# Related info: 
# - https://stackoverflow.com/questions/34203179/fetch-all-maven-dependencies-including-plugin-dependencies
#
# (This one was not enough. Still something was downloaded.
#   RUN mvn -B dependency:resolve dependency:resolve-plugins 
#   RUN mvn -B dependency:copy-dependencies dependency:resolve-plugins dependency:go-offline
# So, instead we just launch tests. Hopefully it downloads every dependency into /root/.m2 directory to be baked in image.)
#
#RUN mvn schema-registry:register --fail-never
RUN mvn verify --fail-never
RUN mvn failsafe:integration-test --fail-never


# Execute our ITs using failsafe plugin in maven. (`mvn failsafe:integration-test`)
#
CMD ["mvn", "verify"]

# Note: In order to speedup a build you may provide a re-used volume where maven will store downloaded dependencies, e.g.:
# docker run --mount source=maven_repository,target=/root/.m2  j9soft/k-repository-it:latest
#
# See also: https://github.com/carlossg/docker-maven