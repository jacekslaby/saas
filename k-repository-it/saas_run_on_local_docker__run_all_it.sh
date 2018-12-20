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
docker-compose up -d zookeeper
docker-compose up -d kafka
docker-compose up -d schema-registry
docker-compose run --rm  k-repository-schemas

# ALTERNATIVE: docker run -it --rm confluentinc/cp-enterprise-kafka:5.0.1

# Note: Topics required by k-repository are auto created in image 'kafka'.
# (Note: in Production of course a different configuration is needed, e.g. number of partitions depending on network size.)
#


# Waits for service to start  @TODO probably not needed ?
sleep 30

# Run our application
#
docker-compose up -d k-repository-it

# Run our integration tests
#
docker-compose run --rm  k-repository-it

#
# ( For environement debugging:
#     docker-compose --file k-repository-it/src/test/resources/docker-compose.yml  run --rm --entrypoint bash k-repository-it
# )
# ( Run our integration tests - Alternative with a docker command:
#    docker run -it --rm --mount source=maven_repository,target=/root/.m2  j9soft/k-repository-it:latest
# )

# Stop all the services
docker-compose down
