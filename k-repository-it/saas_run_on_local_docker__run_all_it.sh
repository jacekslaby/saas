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

# ALTERNATIVE: docker run -it --rm confluentinc/cp-enterprise-kafka:5.0.1
#  BUT it does not provide automatic topic creation.
#  So, we use wurstmeister/kafka.
#

# Note: Topics required by k-repository are auto created in service 'kafka'. (Image wurstmeister/kafka provides such functionality.)
# (Note: in Production of course a different configuration is needed, e.g. number of topic partitions is different and depends on the telco network size.)
#


# Let's register Avro schemas.  
#
#  (We do not need the container to hang around, so we use 'run' instead of 'up'. 
#   AND we want to see the output:
#   [INFO] Registered subject(com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1) with id 1 version 1
#   [INFO] Registered subject(com.j9soft.krepository.v1.entitiesmodel.EntityV1) with id 2 version 1
#   [INFO] Registered subject ... etc.
#   [INFO] ------------------------------------------------------------------------
#   [INFO] BUILD SUCCESS
#  )
docker-compose run --rm  k-repository-schemas

# Waits for service to start and to be configured.
#   @TODO probably not needed ?
sleep 30

# Run our application
#
docker-compose up -d k-repository

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

