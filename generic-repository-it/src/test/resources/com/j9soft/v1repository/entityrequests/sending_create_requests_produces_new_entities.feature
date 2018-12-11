Feature: Sending create requests produces new entities
  Sending CreateEntityRequests results in new Entities being published to the EntitiesTopic.

  Scenario: Send 2 CreateEntityRequests and receive 2 Entities
    Given Entity SourceAlarm "A" does not exist
    And Entity SourceAlarm "B" does not exist
    And I subscribe to EntitiesTopic
    When I send CreateEntityRequest with SourceAlarm "A"
    And I send CreateEntityRequest with SourceAlarm "B"
    Then I should receive Entity SourceAlarm "A"
    And I should receive Entity SourceAlarm "B"
    And I should not receive any other Entities

  Scenario: Send 2 CreateEntityRequests and have 2 Entities stored in Repository
    Given Entity SourceAlarm "A" does not exist
    And Entity SourceAlarm "B" does not exist
    When I send CreateEntityRequest with SourceAlarm "A"
    And I send CreateEntityRequest with SourceAlarm "B"
    And I poll all Entities of type SourceAlarm from Repository
    Then I should receive Entity SourceAlarm "A"
    And I should receive Entity SourceAlarm "B"
    And I should not receive any other Entities
