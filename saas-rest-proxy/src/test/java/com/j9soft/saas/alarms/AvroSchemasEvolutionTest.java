package com.j9soft.saas.alarms;

import org.apache.avro.Schema;
import org.apache.avro.SchemaValidationException;
import org.apache.avro.SchemaValidator;
import org.apache.avro.SchemaValidatorBuilder;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class AvroSchemasEvolutionTest {

    private static final String CURRENT_SCHEMAS_DIRECTORY = "src/main/avro";

    // We need current schema to be backward and forward compatible to the original schema.
    // So we use mutual read strategy.
    private static SchemaValidator schemaValidator = new SchemaValidatorBuilder().mutualReadStrategy().validateAll();

    @Test
    public void testThatAllSchemasAreFullyCompatible() throws IOException, SchemaValidationException {

        // Find all current schema files.
        //
        File dir = new File(CURRENT_SCHEMAS_DIRECTORY);
        File[] directoryListing = dir.listFiles();
        assertNotNull("Avro schemas files must be located in folder: " + CURRENT_SCHEMAS_DIRECTORY, directoryListing);

        // Iterate over all current schemas. Validate each one against the original schema.
        // (e.g. "src/main/avro/create-entity-request-v1.avsc" is validated against
        //       "src/main/avro-original/create-entity-request-v1.avsc" )
        //
        // (btw: Because we iterate over _current_ schemas
        //   it means that we also verify that an original schema is always added when we add a new schema.)
        //
        for (File currentSchemaFile : directoryListing) {
            validateSchema(currentSchemaFile);
        }

        // Useful links:
        //
        // http://cloudurable.com/blog/kafka-avro-schema-registry/index.html
        //
        // https://github.com/confluentinc/schema-registry/blob/master/client/src/main/java/io/confluent/kafka/schemaregistry/avro/AvroCompatibilityChecker.java
        // https://github.com/confluentinc/schema-registry/blob/master/client/src/main/java/io/confluent/kafka/schemaregistry/client/MockSchemaRegistryClient.java
        //
        // https://github.com/HotelsDotCom/avro-compatibility
        //
        // https://github.com/confluentinc/schema-registry/blob/master/docs/maven-plugin.rst
    }

    private void validateSchema(File currentSchemaFile) throws IOException, SchemaValidationException {

        Schema currentSchema = new Schema.Parser().parse(currentSchemaFile);

        String originalSchemaFilePath = currentSchemaFile.getParent() + "-original/" + currentSchemaFile.getName();
        File originalSchemaFile = new File(originalSchemaFilePath);
        Schema originalSchema;
        try {
            originalSchema = new Schema.Parser().parse(originalSchemaFile);
        } catch (FileNotFoundException e) {
            fail(MessageFormat.format(
                    "Avro original schema file is missing: {0}\nIt must be created because you created a new schema: {1}",
                    originalSchemaFile.getPath(), currentSchemaFile.getName()));
            return; // added because compile complained
        }

        // Validate the schemas.
        //
        assertEquals("Name of a schema cannot be changed (file: " + currentSchemaFile.getPath() + ")",
                originalSchema.getName(), currentSchema.getName());
        assertEquals("Namespace of a schema cannot be changed (file: " + currentSchemaFile.getPath() + ")",
                originalSchema.getNamespace(), currentSchema.getNamespace());
        schemaValidator.validate(currentSchema, Collections.singletonList(originalSchema));
    }
}
