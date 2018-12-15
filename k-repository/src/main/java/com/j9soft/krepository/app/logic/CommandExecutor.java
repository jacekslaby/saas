package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logic to build new value for an Entity based on received Request.
 * Depending on Request an Entity may be: created, deleted, updated.
 *
 * Logic is implemented as ValueJoiner because it is used in KStreams processing.
 * See also:
 * https://kafka.apache.org/21/javadoc/org/apache/kafka/streams/kstream/ValueJoiner.html
 */
public class CommandExecutor implements ValueJoiner<GenericRecord, GenericRecord, EntityV1> {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    @Override
    public EntityV1 apply(GenericRecord command, GenericRecord oldEntityValue) {
        EntityV1 newEntityValue = null;

        logger.info("CommandExecutor.apply: {}", command.getSchema());

        // @TODO the logic

        // @TODO start with unit tests

        return newEntityValue;
    }
}
