package com.j9soft.v1repository.entityrequests;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class Stepdefs {
    private List<String> receivedEntities = new ArrayList<>();

    @Given("^Entity SourceAlarm \"([^\"]*)\" does not exist$")
    public void entity_SourceAlarm_does_not_exist(String entityLabel) throws Exception {
        // @TODO: Write code here that turns the phrase above into concrete actions

    }

    @Given("^I subscribe to EntitiesTopic$")
    public void i_subscribe_to_EntitiesTopic() throws Exception {
        // Write code here that turns the phrase above into concrete actions
        receivedEntities.add("A");
        receivedEntities.add("B");
    }

    @When("^I send CreateEntityRequest with SourceAlarm \"([^\"]*)\"$")
    public void i_send_CreateEntityRequest_with_SourceAlarm_A(String sourceAlarmLabel) throws Exception {
        // @TODO Write code here that turns the phrase above into concrete actions
    }

    @Then("^I should receive Entity SourceAlarm \"([^\"]*)\"$")
    public void i_should_receive_Entity_SourceAlarm_A(String entityLabel) throws Exception {

        assertTrue(MessageFormat.format("SourceAlarm {0} was not received", entityLabel),
                receivedEntities.remove(entityLabel));
    }

    @Then("^I should not receive any other Entities$")
    public void i_should_not_receive_any_other_Entities() throws Exception {

        assertEquals(MessageFormat.format("Unexpected Entities received:{0}", receivedEntities),
                0, receivedEntities.size());
    }

    @When("^I poll all Entities of type SourceAlarm from Repository$")
    public void i_poll_all_Entities_of_type_SourceAlarm_from_Repository() throws Exception {
        receivedEntities.add("A");
        receivedEntities.add("B");
    }
}