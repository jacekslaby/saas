package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.entitiesmodel.EntityV1;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import java.util.Collections;

/**
 * Logic to ignore some special Entity objects.
 *
 * Logic is implemented as KeyValueMapper because it is used in KStreams processing.
 * See also:
 * - https://kafka.apache.org/21/documentation/streams/developer-guide/dsl-api.html#stateless-transformations
 *    "FlatMap - Takes one record and produces zero, one, or more records. You can modify the record keys and values, including their types."
 * - https://kafka.apache.org/21/javadoc/org/apache/kafka/streams/kstream/KStream.html#flatMap-org.apache.kafka.streams.kstream.KeyValueMapper-
 */
public class EntityKeyValueMapper implements KeyValueMapper<String, EntityV1, Iterable<KeyValue<String, EntityV1>>> {

    @Override
    public Iterable<KeyValue<String, EntityV1>> apply(String oldKey, EntityV1 entityV1) {

        if (SpecialMarkers.checkIfEntityShouldBeIgnored(entityV1)) {
            return Collections.emptyList();
        }

        return Collections.singletonList(
                new KeyValue<>(oldKey, entityV1) );
    }
}
