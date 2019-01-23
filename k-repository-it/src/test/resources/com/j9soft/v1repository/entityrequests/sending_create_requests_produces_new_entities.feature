Feature: Sending create requests produces new entities
  Sending CreateEntityRequests to CommandsTopic results in new Entities being published to the EntitiesTopic by the logic of k-repository.
  Each Entity has a Type.
  Each Entity belongs to exactly one Subdomain. Each Entity has a unique Id within its Subdomain. Id is string.
  Entity unique key consists of its Subdomain and Id. CreateEntityRequest with an already existing Entity is ignored.
  (@TODO Entity unique key also contains Type.)
  Entity may have zero or more attributes populated. Entities of the same Type may have different attributes.
  It is possible to define attributes by enhancing Avro schema of CreateEntityRequest. Every attribute is optional (i.e. nullable).
  EntitiesTopic is partitioned by Entity unique key, i.e. all changes of an Entity are saved in the same partition.
  EntitiesTopic is a compacted topic, i.e. when an Entity is deleted there is a tombstone (i.e. null) value published.
  EntitiesTopic contains records with key containing Entity unique key and with value containing Entity.
  CommandsTopic contains records with key containing request UUID and with value containing request. CommandsTopic is not compacted.
  Sending DeleteEntityRequests to CommandsTopic results in tombstones being published to the EntitiesTopic by the logic of k-repository.

  Background:
    Given I am connected as producer to CommandsTopic
    And I am connected as consumer to EntitiesTopic

  Scenario: Send 2 CreateEntityRequests and receive 2 Entities
    Given Entity SourceAlarm "A" does not exist
    And Entity SourceAlarm "B" does not exist
    And I skip old Entities and I listen for new Entities on EntitiesTopic
    When I send CreateEntityRequest with SourceAlarm "A"
    And I send CreateEntityRequest with SourceAlarm "B"
    And I waited enough
    And I poll for new Entities from Repository
    Then I should receive 2 Entities
    And I should receive Entity SourceAlarm "A"
    And I should receive Entity SourceAlarm "B"

  Scenario: Send 2 CreateEntityRequests and have 2 Entities stored in Repository
    Given no Entity SourceAlarm exists
    When I send CreateEntityRequest with SourceAlarm "A"
    And I send CreateEntityRequest with SourceAlarm "B"
    And I waited enough
    And I poll all existing Entities from Repository
    Then I should receive 2 Entities
    And I should receive Entity SourceAlarm "A"
    And I should receive Entity SourceAlarm "B"

  Scenario: Send 2 CreateEntityRequests with the same IDs but different Subdomains and receive 2 Entities
    Given Entity SourceAlarm "AinSubdomainX" does not exist
    And Entity SourceAlarm "AinSubdomainY" does not exist
    And I skip old Entities and I listen for new Entities on EntitiesTopic
    When I send CreateEntityRequest with SourceAlarm "AinSubdomainX"
    And I send CreateEntityRequest with SourceAlarm "AinSubdomainY"
    And I waited enough
    And I poll for new Entities from Repository
    Then I should receive 2 Entities
    And I should receive Entity SourceAlarm "AinSubdomainX"
    And I should receive Entity SourceAlarm "AinSubdomainY"

