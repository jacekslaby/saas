docker-compose up -d zookeeper
docker-compose up -d kafka

ping -n 21 127.0.0.1

docker-compose up -d schema-registry

ping -n 21 127.0.0.1

docker-compose run --rm  k-repository-schemas

ping -n 5 127.0.0.1
