

import mock
import os
from register_service_lambda import register_service_lambda

# Following: 
#  https://stackoverflow.com/questions/37143597/mocking-boto3-s3-client-method-python
#  ' Will this approach mock boto clients that are imported in other files? '  (It does not work, so I had to use the next idea:
#  ' It took me a while to realize that a lot of the boto3 clients are effectively generated at runtime, and as such, can't be mocked directly. '
#
import botocore
orig = botocore.client.BaseClient._make_api_call

# Set a region because boto3 requires a non empty value.
#  (It is not used when running the tests anyway, because of mocking. When executed within AWS Lambda the region is provided by AWS.)
# See also: https://stackoverflow.com/questions/40377662/boto3-client-noregionerror-you-must-specify-a-region-error-only-sometimes
#
os.environ['AWS_DEFAULT_REGION'] = 'dummy'

# Event to mock scenario when a container (task) is created.
# (copied from: https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ecs_cwe_events.html )
#
event_about_container_running = {
  "version": "0",
  "id": "9bcdac79-b31f-4d3d-9410-fbd727c29fab",
  "detail-type": "ECS Task State Change",
  "source": "aws.ecs",
  "account": "111122223333",
  "time": "2016-12-06T16:41:06Z",
  "region": "us-east-1",
  "resources": [
    "arn:aws:ecs:us-east-1:111122223333:task/b99d40b3-5176-4f71-9a52-9dbd6f1cebef"
  ],
  "detail": {
    "clusterArn": "arn:aws:ecs:us-east-1:111122223333:cluster/default",
    "containerInstanceArn": "arn:aws:ecs:us-east-1:111122223333:container-instance/b54a2a04-046f-4331-9d74-3f6d7f6ca315",
    "containers": [
      {
        "containerArn": "arn:aws:ecs:us-east-1:111122223333:container/3305bea1-bd16-4217-803d-3e0482170a17",
        "exitCode": 0,
        "lastStatus": "STOPPED",
        "name": "xray",
        "taskArn": "arn:aws:ecs:us-east-1:111122223333:task/b99d40b3-5176-4f71-9a52-9dbd6f1cebef"
      }
    ],
    "createdAt": "2016-12-06T16:41:05.702Z",
    "desiredStatus": "RUNNING",
    "group": "task-group",
    "lastStatus": "RUNNING",
    "overrides": {
      "containerOverrides": [
        {
          "name": "xray"
        }
      ]
    },
    "startedAt": "2016-12-06T16:41:06.8Z",
    "startedBy": "ecs-svc/9223370556150183303",
    "updatedAt": "2016-12-06T16:41:06.975Z",
    "taskArn": "arn:aws:ecs:us-east-1:111122223333:task/b99d40b3-5176-4f71-9a52-9dbd6f1cebef",
    "taskDefinitionArn": "arn:aws:ecs:us-east-1:111122223333:task-definition/xray:2",
    "version": 4
  }
}

def mock_make_api_call(self, operation_name, kwarg):
    print(operation_name)
    if operation_name == 'ChangeResourceRecordSets':
        return 'ala'
    if operation_name == 'DescribeContainerInstances':
        return {'containerInstances': [ {'ec2InstanceId':'@TODO'} ]}
    if operation_name == 'DescribeInstances':
        return {'Reservations': [ {'Instances': [{'PrivateDnsName':'@TODO'}] } ]}
    if operation_name == 'DescribeTaskDefinition':
        return {'taskDefinition': {'containerDefinitions': [{'name':'@TODO'}] } }
    return orig(self, operation_name, kwarg)

@mock.patch.dict(os.environ,{'PRIVATE_ZONE_ID':'dummy_id'})
def test_change_to_running():
    '''verify that event with lastStatus=RUNNING creates a request to add a record in Route 53'''
    
    event = event_about_container_running 
    context = None

    with mock.patch('botocore.client.BaseClient._make_api_call', new=mock_make_api_call):
        register_service_lambda.lambda_handler(event, None)
        
    
    
