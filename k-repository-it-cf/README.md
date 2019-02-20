# K-Repository IT CF

The K-Repository IT CF provides a logic to deploy K-Repository & K-Repository IT via AWS CloudFormation (YAML) on AWS cloud.
Using TaskDefinitions it sets up:
- Kafka (zookeeper + @TODO broker + @TODO schema registry)
- @TODO an instance of K-Repository
- @TODO an instace of K-Repository IT

## Getting Started

```
# Push docker images to AWS ECR.
#
$ docker tag wurstmeister/zookeeper:latest  wurstmeister/zookeeper:3.4.9
$ docker tag wurstmeister/zookeeper:3.4.9  <your aws account such as 123456789012>.dkr.ecr.eu-west-2.amazonaws.com/wurstmeister/zookeeper:3.4.9
$ docker push <your aws account such as 123456789012>.dkr.ecr.eu-west-2.amazonaws.com/wurstmeister/zookeeper:3.4.9

# Put CloudFormation files into s3 bucket.
#
$ aws --profile admin s3api create-bucket --bucket k-repository-it-cf --create-bucket-configuration LocationConstraint=eu-west-2
$ aws --profile admin s3 sync . s3://k-repository-it-cf

# Create stack using AWS Console.

# Start Up the required containers and run ITs.
#
```

## @TODO

ECS does not provide a sensible way for service discovery.
Existing workarounds require a lot of effort to deploy.   https://github.com/nathanpeck/awesome-ecs  Service Discovery

In branch 'add-aws-cf-service-discovery' there is an approach with Route53, Lambda and CloudWatch events.
It is still not finished. 