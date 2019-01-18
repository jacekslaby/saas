Feature: Sending unknown requests produces exceptions
  Sending UknownEntityRequests results in exception being thrown to client code.
  (because a client can only send a request conforming to preconfigured schemas (including list of entity attributes))

  Background:
    Given I am connected as producer to CommandsTopic

  Scenario: Send UnknownEntityRequest and receive exception
    When I send UnknownEntityRequest I should receive exception
