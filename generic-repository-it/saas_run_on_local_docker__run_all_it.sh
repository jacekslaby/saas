# Inspiration: 
#  https://github.com/docker/labs/blob/master/developer-tools/java/chapters/ch09-cicd.adoc
#  "When creating integration tests, it is useful to be able to run and debug them outside Jenkins. 
#   In order to do that, you can simply run the same commands you ran in the Jenkins build"
#
#  https://github.com/confluentinc/examples/blob/5.0.1-post/ccloud/docker-compose.yml
#

# Starts Test Double services for Zookeeper, Kafka broker and Schema Registry
# ( https://docs.confluent.io/current/installation/docker/docs/config-reference.html
#   https://docs.confluent.io/current/installation/docker/docs/image-reference.html
# )
#
docker-compose --file src/test/resources/docker-compose.yml up -d zookeeper
docker-compose --file src/test/resources/docker-compose.yml up -d kafka
docker-compose --file src/test/resources/docker-compose.yml up -d schema-registry

# ALTERNATIVE: docker run -it --rm confluentinc/cp-enterprise-kafka:5.0.1

# Create topics required by k-repository.
# (Note: in Production of course a different configuration is needed, e.g. number of partitions depending on network size.)
#


# Waits for service to start  @TODO probably not needed ?
sleep 30

# Run our application
#@TODO docker-compose --file src/test/resources/docker-compose.yml  run saas-repository

# Run our integration tests
#
#docker-compose --file src/test/resources/docker-compose.yml  run generic-repository-it
docker-compose --file src/test/resources/docker-compose.yml  run --rm  generic-repository-it
#
# ( For environement debugging:
#     docker-compose --file src/test/resources/docker-compose.yml  run --rm --entrypoint bash generic-repository-it
# )
# ( Run our integration tests - Alternative with a docker command:
#    docker run -it --rm --mount source=maven_repository,target=/root/.m2  j9soft/generic-repository-it:latest
# )

# Stop all the services
docker-compose --file src/test/resources/docker-compose.yml down

