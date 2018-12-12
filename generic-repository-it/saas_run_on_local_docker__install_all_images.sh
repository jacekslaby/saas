# https://github.com/docker/labs/blob/master/developer-tools/java/chapters/ch09-cicd.adoc
# When creating integration tests, it is useful to be able to run and debug them outside Jenkins. In order to do that, you can simply run the same commands you ran in the Jenkins build:

# Old approach: docker images settings provided in saas-repository-it/pom.xml
# cd saas-repository-it/
# Generates the images
# mvn -f pom.xml clean install -Papp-docker-image


# Current approach: each required service provides its own Dockerfile.

# Generates the images in Docker
cd ../generic-repository/
mvn clean install docker:build

cd ../generic-repository-it/
mvn clean docker:build
#
# btw: We cannot use the following because it launches the goal `failsafe:integration-test` and we do not want to run our ITs in this moment.
#mvn -f pom.xml clean install
