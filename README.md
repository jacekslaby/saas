# Raas

@TODO

## Streaming Platform (Kafka)

### Schema evolution rules

Avro schemas are used to serialize values send to Kafka topic. (e.g. schemas like `CreateEntityRequestV1`, `ResyncAllStartSubdomainRequestV1`, etc.)

The first version of any schema is "tagged" with suffix 'V1' in schema name. 
**Every** change made to a schema must be FULLy compatible (as understood in this [article](http://cloudurable.com/blog/kafka-avro-schema-registry/index.html)),
i.e. changes are forwards and backwards compatible from latest to new and from new to latest.

Rules (copied from [here](http://cloudurable.com/blog/kafka-avro-schema-registry/index.html)):
- You can add a field with a default to a schema. 
- You can remove a field that had a default value. 
- You can change a field's order attribute. 
- You can change a field's default value to another value or add a default value to a field that did not have one. 
- You can remove or add a field alias (keep in mind that this could break some consumers that depend on the alias). 
- You can change a type to a union that contains original type. 
(Note: Adding a new value to an enum is NOT forward compatible.)

See also:
- section "recommendations specific to Avro" from [Why Avro For Kafka Data?](https://www.confluent.io/blog/avro-kafka-data/)
- [Schema evolution in Avro, Protocol Buffers and Thrift](https://martin.kleppmann.com/2012/12/05/schema-evolution-in-avro-protocol-buffers-thrift.html)

Note: If we need to introduce an incompatible change then we must create a new schema (e.g. `CreateEntityRequestV2`) and provide a translator logic (e.g. docker image) able to continuously receive one version and publish the other. (e.g. If we intend to move all clients to `V2` then we need to provide a container which receives `CreateEntityRequestV1` objects, transforms them and sends as `CreateEntityRequestV2`.)
This approach allows us to migrate all components without a downtime. (We can decomission support for `V1`, including the translator logic, when it is no longer used by any component.)