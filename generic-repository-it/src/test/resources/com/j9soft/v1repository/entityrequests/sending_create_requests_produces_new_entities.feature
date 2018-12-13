Feature: Sending create requests produces new entities
  Sending CreateEntityRequests results in new Entities being published to the EntitiesTopic.

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
    Then I should receive 2 Entities
    And I should receive Entity SourceAlarm "A"
    And I should receive Entity SourceAlarm "B"

  Scenario: Send 2 CreateEntityRequests and have 2 Entities stored in Repository
    Given Entity SourceAlarm "A" does not exist
    And Entity SourceAlarm "B" does not exist
    When I send CreateEntityRequest with SourceAlarm "A"
    And I send CreateEntityRequest with SourceAlarm "B"
    And I poll all existing Entities from Repository
    Then I should receive 2 Entities
    And I should receive Entity SourceAlarm "A"
    And I should receive Entity SourceAlarm "B"
