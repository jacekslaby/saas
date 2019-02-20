
import json
import boto3
import os
def lambda_handler(event, context):
    private_zone_id = os.environ['PRIVATE_ZONE_ID']
    print('PRIVATE_ZONE_ID: ' + private_zone_id) 
    print('Received event: ' + json.dumps(event, indent=2)) 
    
    private_zone_suffix = '@TODO .internal.'
    
    ecs_client = boto3.client('ecs')
    
    # DescribeTaskDefinition - let's retrieve name(s) of the container(s) as specified in TaskDefinition
    # https://boto3.amazonaws.com/v1/documentation/api/latest/reference/services/ecs.html#ECS.Client.describe_task_definition
    task_definition_id = event['detail']['taskDefinitionArn']
    task_definition = ecs_client.describe_task_definition(taskDefinition=task_definition_id)
    container_names = [ container_definition['name'] for container_definition in task_definition['taskDefinition']['containerDefinitions'] ]
    # @TODO add support for many containers in TaskDefinition
    service_dns_name = container_names[0] + private_zone_suffix
    
    # DescribeContainerInstances - get ID of our EC2 instance
    # https://docs.aws.amazon.com/AmazonECS/latest/APIReference/API_DescribeContainerInstances.html
    # https://boto3.amazonaws.com/v1/documentation/api/latest/reference/services/ecs.html#ECS.Client.describe_container_instances
    #
    container_instances = ecs_client.describe_container_instances(cluster='string', containerInstances=['string'])
    ec2_instance_id = container_instances['containerInstances'][0]['ec2InstanceId'];

    # DescribeInstances - get private DNS name for our EC2 instance
    # https://boto3.amazonaws.com/v1/documentation/api/latest/reference/services/ec2.html#EC2.Client.describe_instances
    ec2_client = boto3.client('ec2')
    reservations = ec2_client.describe_instances(InstanceIds=[ec2_instance_id])
    ec2_instance_private_dns_name = reservations['Reservations'][0]['Instances'][0]['PrivateDnsName']
    
    # ChangeResourceRecordSets - let's register that our service DNS name points to EC2 instance private DNS name
    route53_client = boto3.client('route53')
    response = route53_client.change_resource_record_sets(
        HostedZoneId=private_zone_id,
        ChangeBatch={'Comment': 'ECS service registered', 'Changes': [
                        {'Action': 'UPSERT', 'ResourceRecordSet': {
                            'Name': service_dns_name, 'Type': 'CNAME', 'TTL': 60, 'ResourceRecords': [{'Value' : ec2_instance_private_dns_name}]}
                        }]
                    }
        )
    print(response)
    