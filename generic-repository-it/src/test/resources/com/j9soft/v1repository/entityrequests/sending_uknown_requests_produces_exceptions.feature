Feature: Sending unknown requests produces exceptions
  Sending UknownEntityRequests results in exception being thrown to client code.

  Background:
    Given I am connected as producer to CommandsTopic

  Scenario: Send UknownEntityRequest and receive exception
    When I send UknownEntityRequest I should receive exception
