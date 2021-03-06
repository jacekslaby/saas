package com.j9soft.saas.alarms.testdata;

import com.j9soft.krepository.v1.commandsmodel.CreateEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.DeleteEntityRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllEndSubdomainRequestV1;
import com.j9soft.krepository.v1.commandsmodel.ResyncAllStartSubdomainRequestV1;
import com.j9soft.saas.alarms.dao.RequestDao;
import com.j9soft.saas.alarms.model.Definitions;

import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

/**
 * Class used as a visitor to captured Requests (which are generated by the tested logic).
 * During a visit (i.e. inside method saveInDao() which invokes method RequestDao.saveNewRequest() )
 * this class verifies correctness of the DAO request. (e.g. of a CreateEntityRequestV1, DeleteEntityRequestV1, etc.)
 */
public class CapturedRequestChecker implements RequestDao {

    private CreateEntityRequestV1 expectedCreateEntityRequest;
    private DeleteEntityRequestV1 expectedDeleteEntityRequest;
    private ResyncAllStartSubdomainRequestV1 expectedResyncAllStartSubdomainRequest;
    private ResyncAllEndSubdomainRequestV1 expectedResyncAllEndSubdomainRequest;

    public static CapturedRequestChecker newBuilder() {
        return new CapturedRequestChecker();
    }

    public CapturedRequestChecker addCreateEntityRequest(CreateEntityRequestV1 expectedRequest) {
        this.expectedCreateEntityRequest = expectedRequest;
        return this;
    }

    public CapturedRequestChecker addDeleteEntityRequest(DeleteEntityRequestV1 expectedRequest) {
        this.expectedDeleteEntityRequest = expectedRequest;
        return this;
    }

    public CapturedRequestChecker addResyncAllStartSubdomainRequest(ResyncAllStartSubdomainRequestV1 expectedRequest) {
        this.expectedResyncAllStartSubdomainRequest = expectedRequest;
        return this;
    }

    public CapturedRequestChecker addResyncAllEndSubdomainRequest(ResyncAllEndSubdomainRequestV1 expectedRequest) {
        this.expectedResyncAllEndSubdomainRequest = expectedRequest;
        return this;
    }

    @Override
    public void saveNewRequest(CreateEntityRequestV1 capturedRequest, RequestDao.Callback callback) {
        // - some fields should be equal
        // - other fields should be auto-generated

        // Check fields that should be equal.
        org.assertj.core.api.Assertions.assertThat(capturedRequest)
                .isEqualToIgnoringGivenFields(
                        expectedCreateEntityRequest,
                        Definitions.DAO_SCHEMA_REQUEST__UUID,
                        Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE,
                        Definitions.DAO_SCHEMA_REQUEST__EVENT_DATE);

        // Check fields that should be auto-generated.
        assertTrue("proper uuid should be generated",
                UUID.fromString(capturedRequest.getUuid().toString()).version() > 0);

        assertThat(Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE,
                capturedRequest.getEntryDate(), lessThanOrEqualTo(System.currentTimeMillis()));
    }

    @Override
    public void saveNewRequest(DeleteEntityRequestV1 capturedRequest, RequestDao.Callback callback) {
        // - some fields should be equal
        // - other fields should be auto-generated

        // Check fields that should be equal.
        org.assertj.core.api.Assertions.assertThat(capturedRequest)
                .isEqualToIgnoringGivenFields(
                        expectedDeleteEntityRequest,
                        Definitions.DAO_SCHEMA_REQUEST__UUID,
                        Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE);

        // Check fields that should be auto-generated.
        assertTrue("proper uuid should be generated",
                UUID.fromString(capturedRequest.getUuid().toString()).version() > 0);

        assertThat(Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE,
                capturedRequest.getEntryDate(), lessThanOrEqualTo(System.currentTimeMillis()));

    }

    @Override
    public void saveNewRequest(ResyncAllStartSubdomainRequestV1 capturedRequest, RequestDao.Callback callback) {
        // - some fields should be equal
        // - other fields should be auto-generated

        // Check fields that should be equal.
        org.assertj.core.api.Assertions.assertThat(capturedRequest)
                .isEqualToIgnoringGivenFields(
                        expectedResyncAllStartSubdomainRequest,
                        Definitions.DAO_SCHEMA_REQUEST__UUID,
                        Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE,
                        Definitions.DAO_SCHEMA_REQUEST__EVENT_DATE);

        // Check fields that should be auto-generated.
        verifyUuidAndEntryDate(capturedRequest.getUuid(), capturedRequest.getEntryDate());
    }

    @Override
    public void saveNewRequest(ResyncAllEndSubdomainRequestV1 capturedRequest, RequestDao.Callback callback) {
        // - some fields should be equal
        // - other fields should be auto-generated

        org.assertj.core.api.Assertions.assertThat(capturedRequest).isInstanceOf(   // @TODO come with something better so that compiler discovers copy&paste errors with expectedResyncAllEndSubdomainRequest
                expectedResyncAllEndSubdomainRequest.getClass());

        // Check fields that should be equal.
        org.assertj.core.api.Assertions.assertThat(capturedRequest)
                .isEqualToIgnoringGivenFields(
                        expectedResyncAllEndSubdomainRequest,
                        Definitions.DAO_SCHEMA_REQUEST__UUID,
                        Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE,
                        Definitions.DAO_SCHEMA_REQUEST__EVENT_DATE);

        // Check fields that should be auto-generated.
        verifyUuidAndEntryDate(capturedRequest.getUuid(), capturedRequest.getEntryDate());
    }

    private void verifyUuidAndEntryDate(CharSequence uuid, Long entryDateAsMillis) {
        assertTrue("proper uuid should be generated", UUID.fromString(uuid.toString()).version() > 0);
        assertThat(Definitions.DAO_SCHEMA_REQUEST__ENTRY_DATE, entryDateAsMillis, lessThanOrEqualTo(System.currentTimeMillis()));
    }

}
