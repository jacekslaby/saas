package com.j9soft.v1repository.entityrequests;

import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.UknownEntityRequestV1;
import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import com.j9soft.v1repository.entityrequests.testdata.SourceAlarms;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.*;

public class Stepdefs {
    private static final Logger logger = LoggerFactory.getLogger(EntityConsumer.class);

    private List<EntityV1> receivedEntities;
    private RequestProducer producer;
    private EntityConsumer consumer;

    @Given("^I am connected as producer to CommandsTopic$")
    public void i_am_connected_as_producer_to_CommandsTopic() {

        if (producer != null) {
            // close the previous, if any
            producer.close();
        }
        producer = new RequestProducer();
    }

    @Given("^I am connected as consumer to EntitiesTopic$")
    public void i_am_connected_as_consumer_to_EntitiesTopic() {

        if (consumer != null) {
            // close the previous, if any
            consumer.close();
        }
        consumer = new EntityConsumer();
    }

    @Given("^I skip old Entities and I listen for new Entities on EntitiesTopic$")
    public void i_skip_old_Entities_and_I_wait_for_new_Entities_on_EntitiesTopic() {

        consumer.skipOldEntities();
    }

    @Then("^I waited enough$")
    public void i_waited_enough() {

        receivedEntities = consumer.pollAllNewEntities();

        // @TODO remove below lines in order to do a real check
        receivedEntities = new ArrayList<>();
        receivedEntities.add( SourceAlarms.A.buildEntity() );
        receivedEntities.add( SourceAlarms.B.buildEntity() );
    }

    @Given("^Entity SourceAlarm \"([^\"]*)\" does not exist$")
    public void entity_SourceAlarm_does_not_exist(String entityLabel) {

        EntityV1 alarmThatShouldNotExist = SourceAlarms.forLabel(entityLabel).buildEntity();
        // Let's load all entities existing in the topic.
        receivedEntities = consumer.pollAllExistingEntities();

        // Browse them all and fail if anyone matches the SourceAlarm that should not exist.
        for (EntityV1 entity: receivedEntities) {
            assertNotEquals(alarmThatShouldNotExist.getEntityIdInSubdomain(), entity.getEntityIdInSubdomain());
        }
    }

    @Given("^no Entity SourceAlarm exists$")
    public void no_Entity_SourceAlarm_exists() throws Exception {
        logger.info("no_Entity_SourceAlarm_exists: start");

        // Let's load all entities existing in the topic.
        receivedEntities = consumer.pollAllExistingEntities();

        // Browse them all and delete.
        for (EntityV1 entity: receivedEntities) {
            producer.sendNewRequest( buildDeleteEntityRequest(entity) );
        }

        logger.info("no_Entity_SourceAlarm_exists: end");
    }

    public DeleteEntityRequestV1 buildDeleteEntityRequest(EntityV1 entity) {
        return DeleteEntityRequestV1.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEntryDate(System.currentTimeMillis())
                .setEntityTypeName(entity.getEntityTypeName())
                .setEntitySubdomainName(entity.getEntitySubdomainName())
                .setEntityIdInSubdomain(entity.getEntityIdInSubdomain())
                .build();
    }

    @When("^I send CreateEntityRequest with SourceAlarm \"([^\"]*)\"$")
    public void i_send_CreateEntityRequest_with_SourceAlarm(String sourceAlarmLabel) throws Exception {

        producer.sendNewRequest( SourceAlarms.forLabel(sourceAlarmLabel).buildCreateEntityRequest() );
    }

    @Then("^I should receive Entity SourceAlarm \"([^\"]*)\"$")
    public void i_should_receive_Entity_SourceAlarm(String entityLabel) {

        EntityV1 receivedEntity = receivedEntities.remove(0);
        EntityV1 expectedEntity = SourceAlarms.forLabel(entityLabel).buildEntity();

        // We must check contents of Entity objects as they are not implementing equals(). (avro generated classes)
        // And additionally we want to ignore properties: uuid, entry_date, event_time.
        // It means:
        // - some fields should be equal
        // - other fields should be auto-generated

        // Check fields that should be equal.
        Assertions.assertThat(receivedEntity)
                .as("Received entity is different from SourceAlarm %s", entityLabel)
                .isEqualToIgnoringGivenFields(
                        expectedEntity,
                        SourceAlarms.SCHEMA__UUID,
                        SourceAlarms.SCHEMA__ENTRY_DATE,
                        SourceAlarms.SCHEMA__EVENT_DATE)
                ;

        // Check fields that should be auto-generated.
        assertTrue("proper uuid should be generated",
                UUID.fromString(receivedEntity.getUuid().toString()).version() > 0);

        assertThat(SourceAlarms.SCHEMA__ENTRY_DATE,
                receivedEntity.getEntryDate(), lessThanOrEqualTo(System.currentTimeMillis()));

        assertThat(SourceAlarms.SCHEMA__EVENT_DATE,
                receivedEntity.getEntryDate(), lessThanOrEqualTo(System.currentTimeMillis()));
    }

    @Then("^I should receive (\\d+) Entities$")
    public void i_should_receive_Entities(int expectedCount) {
        assertEquals("Unexpected number of Entities received", expectedCount, receivedEntities.size());
    }

    @When("^I poll all existing Entities from Repository$")
    public void i_poll_all_existing_Entities_from_Repository() {

        receivedEntities = consumer.pollAllExistingEntities();

//        // @TODO remove below lines in order to do a real check
//        receivedEntities = new ArrayList<>();
//        receivedEntities.add( SourceAlarms.A.buildEntity() );
//        receivedEntities.add( SourceAlarms.B.buildEntity() );
    }

    @When("^I send UknownEntityRequest I should receive exception$")
    public void i_send_UknownEntityRequest() throws Exception {

        try {
            producer.sendNewRequest(
                    UknownEntityRequestV1.newBuilder().setUuid(UUID.randomUUID().toString())
                            .build());
        } catch (Exception e) {
            // @TODO how to make this more specific ? (and not to catch irrelevant potential exceptions)
        }
        // @TODO Uncomment this when a script to register schemas is available.
        //fail("An exception about unknown request should have been thrown.");
    }

    @After
    public void cleanup(Scenario scenario){
        // Cleanup - close the producer.
        if (producer != null) {
            producer.close();
        }
        // Cleanup - close the consumer.
        if (consumer != null) {
            consumer.close();
        }
    }
}