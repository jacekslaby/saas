#!/bin/sh
# start-k-repository.sh 
#
# Related info about 'health checking':
# - https://stackoverflow.com/questions/31746182/docker-compose-wait-for-container-x-before-starting-y/41854997#41854997
#   "In the Dockerfiles CMD you could refer to your own start script that wraps starting up your container service." 
# - https://docs.docker.com/compose/startup-order/
#   "write your own wrapper script to perform a more application-specific health check"   



# Wait for schema registry to become available.
# (Otherwise a faulty client might be the first to register a schema and it may provide a schema which is not compatible with minimal schema required by k-repository.)
#
# @TODO: use env variables KR_SCHEMA_REGISTRY_HOST, KR_SCHEMA_REGISTRY_PORT
#
SCHEMA_REGISTRY_URL=schema-registry:8081
while ! nc -z schema-registry 8081;
do
  >&2 echo "INFO: Schema registry is unavailable - sleeping...  (schema registry at $SCHEMA_REGISTRY_URL)"
  sleep 1;
done;

# Let's register Avro schemas.
#
# Related info about registering schemas:
# - Using curl: https://github.com/confluentinc/schema-registry/issues/789 
# - Adding newline after curl's output: https://stackoverflow.com/questions/12849584/automatically-add-newline-at-end-of-curl-response-body
#
>&2 echo "INFO: Schema registry is up and running - registering minimal schemas now...  (schema registry at $SCHEMA_REGISTRY_URL)"
for file_with_schema in /k-repository-schemas/*.avsc
do
  NAMESPACE=$(jq -r '.namespace' $file_with_schema)
  NAME=$(jq -r '.name' $file_with_schema)
  SUBJECT=$NAMESPACE.$NAME
  >&2 echo "INFO: registering schema for subject: $SUBJECT"
  SCHEMA=$(jq tostring $file_with_schema)
  curl -XPOST -H "Content-Type: application/vnd.schemaregistry.v1+json" -d"{\"schema\":$SCHEMA}" http://$SCHEMA_REGISTRY_URL/subjects/$SUBJECT/versions ; echo
done

# @TODO: wait for topics to be available. (They are auto created by wurstmeister image.)
# (Otherwise k-repository image fails with:
#   " prodxphone-saas-v1-commands-topic is unknown yet during rebalance, please make sure they have been pre-created before starting the Streams application. " )
#
# Note: We do not use bin/kafka-topics.sh because we would have to bake kafka binaries into our docker image.
#   Instead we use a standalone java app. (Not sure if it is smaller...)
#
# Related info about bin/kafka-topics.sh:
# - https://stackoverflow.com/questions/40034074/kafka-topics-sh-delete-topic-testtopic-is-not-working-for-kafka-v-0-10
#   bin/kafka-topics.sh --list --zookeeper localhost:2181
#   (in wurstmeister's container: docker exec -it kafka    /opt/kafka_2.12-2.1.0/bin/kafka-topics.sh --list --zookeeper zookeeper:2181 )
# - https://howtoprogram.xyz/2016/07/08/apache-kafka-command-line-interface/
#   bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic hello-topic
#   (in wurstmeister's container: docker exec -it kafka    /opt/kafka_2.12-2.1.0/bin/kafka-topics.sh --describe --zookeeper zookeeper:2181 --topic prodxphone-saas-v1-commands-topic )
#
# @TODO: use KR_REPOSITORY_NAME as prefix for expected topics.
# @TODO: use KR_ZOOKEEPER_URL.
# 
ZOOKEEPER_URL=zookeeper:2181
>&2 echo "INFO: Checking for topics...  (with zookeeper at $ZOOKEEPER_URL)"
for topic in prodxphone-saas-v1-commands-topic prodxphone-saas-v1-entities-topic
do
  while ! java -jar /tools/kafka-topics-jar-with-dependencies.jar  --describe --zookeeper $ZOOKEEPER_URL --topic $topic
  do
    >&2 echo "INFO: Topic '$topic' is not available - sleeping..."
    sleep 1;
  done;
  >&2 echo "INFO: Available topic: $topic"
done


>&2 echo "INFO: Schema registry & Avro schemas are ready, topics are ready - starting k-repository now..."

cd /app
exec java -jar k-repository.jar
