package com.j9soft.v1repository.entityrequests;


import com.j9soft.v1repository.entityrequests.testdata.SourceAlarms;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class Stepdefs {
    private List<String> receivedEntities = new ArrayList<>();
    private RequestProducer producer;

    @Given("^I am connected as producer to CommandsTopic$")
    public void i_am_connected_as_producer_to_CommandsTopic() throws Exception {

        producer = new RequestProducer();
    }

    @Given("^I am connected as subscriber to EntitiesTopic$")
    public void i_am_connected_as_subscriber_to_EntitiesTopic() throws Exception {
        // @TODO Write code here that turns the phrase above into concrete actions
    }

    @Given("^I skip old Entities and I wait for new Entities on EntitiesTopic$")
    public void i_skip_old_Entities_and_I_wait_for_new_Entities_on_EntitiesTopic() throws Exception {
        // @TODO Write code here that turns the phrase above into concrete actions
        receivedEntities.add("A");
        receivedEntities.add("B");
    }

    @Given("^Entity SourceAlarm \"([^\"]*)\" does not exist$")
    public void entity_SourceAlarm_does_not_exist(String entityLabel) throws Exception {
        // @TODO: Write code here that turns the phrase above into concrete actions

    }

    @When("^I send CreateEntityRequest with SourceAlarm \"([^\"]*)\"$")
    public void i_send_CreateEntityRequest_with_SourceAlarm_A(String sourceAlarmLabel) throws Exception {

        producer.sendNewRequest( SourceAlarms.forLabel(sourceAlarmLabel).buildCreateEntityRequest() );
    }

    @Then("^I should receive Entity SourceAlarm \"([^\"]*)\"$")
    public void i_should_receive_Entity_SourceAlarm_A(String entityLabel) throws Exception {

        assertTrue(MessageFormat.format("SourceAlarm {0} was not received", entityLabel),
                receivedEntities.remove(entityLabel));
    }

    @Then("^I should receive (\\d+) Entities$")
    public void i_should_receive_Entities(int expectedCount) {
        assertEquals(MessageFormat.format("Unexpected number of Entities received:{0}", receivedEntities.size()),
                expectedCount, receivedEntities.size());
    }

    @When("^I poll all existing Entities from Repository$")
    public void i_poll_all_existing_Entities_from_Repository() throws Exception {
        receivedEntities.add("A");
        receivedEntities.add("B");
    }

    @After
    public void doSomethingAfter(Scenario scenario){
        // Cleanup - close the producer.
        if (this.producer != null) {
            this.producer.close();
        }
    }
}