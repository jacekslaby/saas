Feature: Sending resync requests removes obsolete entities
  Sending ResyncAllStartSubdomainRequest followed by ResyncAllEndSubdomainRequest results in old Entities being deleted from the EntitiesTopic.
  For every deleted Entity a DeleteEntityRequest is published to EntitiesTopic.
  Resynchronization is done within set of Entities from a Subdomain specified in ResyncAllStartSubdomainRequest and ResyncAllEndSubdomainRequest.
  Entities created and updated between start and end of a resynchronization are not deleted, i.e. they are not perceived as obsolete ones.

  Background:
    Given I am connected as producer to CommandsTopic
    And I am connected as consumer to EntitiesTopic

  Scenario: Send 2 CreateEntityRequests followed by resync with 1 CreateEntityRequest and have 1 Entity stored in Repository
    Given no Entity SourceAlarm exists
    And I send CreateEntityRequest with SourceAlarm "AinSubdomainX"
    And I send CreateEntityRequest with SourceAlarm "BinSubdomainX"
    And I send ResyncAllStartSubdomainRequest for Subdomain "X"
    And I send CreateEntityRequest with SourceAlarm "CinSubdomainX"
    And I send ResyncAllEndSubdomainRequest for Subdomain "X"
    And I waited enough
    And I poll all existing Entities from Repository
    Then I should receive 1 Entities
    And I should receive Entity SourceAlarm "CinSubdomainX"

  Scenario: Send 2 CreateEntityRequests followed by resync in another subdomain and have 2 Entities stored in Repository
    Given no Entity SourceAlarm exists
    And I send CreateEntityRequest with SourceAlarm "AinSubdomainX"
    And I send CreateEntityRequest with SourceAlarm "BinSubdomainX"
    And I send CreateEntityRequest with SourceAlarm "DinSubdomainY"
    And I send ResyncAllStartSubdomainRequest for Subdomain "Y"
    And I send ResyncAllEndSubdomainRequest for Subdomain "Y"
    And I waited enough
    And I poll all existing Entities from Repository
    Then I should receive 2 Entities
    And I should receive Entity SourceAlarm "AinSubdomainX"
    And I should receive Entity SourceAlarm "BinSubdomainX"

