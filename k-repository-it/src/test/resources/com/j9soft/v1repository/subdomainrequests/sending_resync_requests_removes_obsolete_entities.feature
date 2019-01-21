Feature: Sending resync requests removes obsolete entities
  Sending ResyncAllStartSubdomainRequest followed by ResyncAllEndSubdomainRequest results in old Entities being removed from the EntitiesTopic.

  Background:
    Given I am connected as producer to CommandsTopic
    And I am connected as consumer to EntitiesTopic

  Scenario: Send 2 CreateEntityRequests followed by resync with 1 CreateEntityRequest and have 1 Entity stored in Repository
    Given no Entity SourceAlarm exists
    And I send CreateEntityRequest with SourceAlarm "A" in Subdomain "X"
    And I send CreateEntityRequest with SourceAlarm "B" in Subdomain "X"
    And I send ResyncAllStartSubdomainRequest for Subdomain "X"
    And I send CreateEntityRequest with SourceAlarm "C" in Subdomain "X"
    And I send ResyncAllEndSubdomainRequest for Subdomain "X"
    And I waited enough
    And I poll all existing Entities from Repository
    Then I should receive 1 Entities
    And I should receive Entity SourceAlarm "C"

  Scenario: Send 2 CreateEntityRequests followed by resync in another subdomain and have 2 Entities stored in Repository
    Given no Entity SourceAlarm exists
    And I send CreateEntityRequest with SourceAlarm "A" in Subdomain "X"
    And I send CreateEntityRequest with SourceAlarm "B" in Subdomain "X"
    And I send CreateEntityRequest with SourceAlarm "D" in Subdomain "Y"
    And I send ResyncAllStartSubdomainRequest for Subdomain "Y"
    And I send ResyncAllEndSubdomainRequest for Subdomain "Y"
    And I waited enough
    And I poll all existing Entities from Repository
    Then I should receive 2 Entities
    And I should receive Entity SourceAlarm "A"
    And I should receive Entity SourceAlarm "B"

