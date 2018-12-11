# Inspiration: 
#  https://github.com/docker/labs/blob/master/developer-tools/java/chapters/ch09-cicd.adoc
#  "When creating integration tests, it is useful to be able to run and debug them outside Jenkins. 
#   In order to do that, you can simply run the same commands you ran in the Jenkins build"
#

# Starts service: test-double-kafka-broker
docker-compose --file src/test/resources/docker-compose.yml up -d test-double-kafka-broker
# old approach: docker-compose --file saas-repository-it/src/test/resources/docker-compose.yml up -d test-double-kafka-broker

# Waits for service to start  @TODO probably not needed, as we use an embedded kafka broker, so it is available "immediately <5s" ?
sleep 30

# Run our application
#docker-compose --file src/test/resources/docker-compose.yml \
               #3run saas-repository \
               #java -jar /maven/jar/mongo-docker-demo-1.0-SNAPSHOT-jar-with-dependencies.jar mongo
docker-compose --file src/test/resources/docker-compose.yml  run saas-repository 

# Run our integration tests
#
#docker-compose --file src/test/resources/docker-compose.yml \
               #run saas-repository-it mvn -f /maven/code/pom.xml \
               #-Dmaven.repo.local=/m2/repository -Pintegration-test verify
#
#docker-compose --file src/test/resources/docker-compose.yml  run generic-repository-it
docker-compose --file src/test/resources/docker-compose.yml  run --rm  generic-repository-it
#
# ( Run our integration tests - Alternative with a docker command:
#    docker run -it --rm --mount source=maven_repository,target=/root/.m2  j9soft/generic-repository-it:latest
# )

# Stop all the services
docker-compose --file src/test/resources/docker-compose.yml down

