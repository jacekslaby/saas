package com.j9soft.saas.alarms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.j9soft.saas.alarms.model.RequestDto;
import com.j9soft.saas.alarms.model.RequestsListDto;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 * Tests to validate that JSON annotations are properly defined in classes parsing body of a REST request.
 */
public class RequestsListDtoTest {

    @Test
    public void test_whenCorrectJson_itIsCorrectlyDeserialized() throws IOException {

        parseJsonAndValidate("src/test/resources/correctJsonForRequestsList.json");
    }

    @Test
    public void test_whenIncorrectJson_itCausesDeserializationException() throws IOException {

        try {
            parseJsonAndValidate("src/test/resources/incorrectJsonForRequestsList.json");
            fail("Incorrect JSON should not be allowed.");
        } catch (UnrecognizedPropertyException e) {
            // It was expected, so nothing is done.
        }

        try {
            parseJsonAndValidate("src/test/resources/secondIncorrectJsonForRequestsList.json");
            fail("Incorrect JSON should not be allowed.");
        } catch (InvalidTypeIdException e) {
            // It was expected, so nothing is done.
        }

        // We expect violation containing the following strings:
        String[] expectedInErrorMessage = new String[]{"requests_array", "must not be null"};
        parseJsonAndValidate(expectedInErrorMessage,"src/test/resources/thirdIncorrectJsonForRequestsList.json");
    }

    private void parseJsonAndValidate(String pathToFileWithJson) throws IOException {
        // No violation is expected,  (however a parse exception can happen - these are two different things)
        //  so we put null as the first parameter.
        parseJsonAndValidate(null, pathToFileWithJson);
    }

    /**
     * Parse JSON from a file and throw exception if it is not matching the schema.
     * Additionally, if first parameter is not null, a violation is expected to be detected - tests fail if expected violation does not occur.
     */
    private void parseJsonAndValidate(String[] expectedStringsInViolationMessage, String pathToFileWithJson) throws IOException {
        // Read file contents.
        File jsonFile = new File(pathToFileWithJson);
        String jsonString = new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8);

        // Parse JSON to java object, i.e. to a requestListDto.
        RequestsListDto requestListDto;
        ObjectMapper mapper = new ObjectMapper();
        requestListDto = mapper.readValue(jsonString, RequestsListDto.class);

        // Validate the new requestDto, according to validation rules. (e.g. check @NotNull annotations)
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<RequestsListDto>> violations = validator.validate(requestListDto);
        if (expectedStringsInViolationMessage == null) {
            assertEquals(0, violations.size());
        } else {
            assertEquals(1, violations.size());
            String violationMessage = violations.iterator().next().toString();
            for (String expectedString: expectedStringsInViolationMessage) {
                assertThat(violationMessage, containsString(expectedString));
            }
        }
    }

}
