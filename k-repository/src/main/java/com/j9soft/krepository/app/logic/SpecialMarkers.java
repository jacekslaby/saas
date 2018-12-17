package com.j9soft.krepository.app.logic;

import com.j9soft.krepository.v1.entitiesmodel.EntityV1;

public class SpecialMarkers {

    // Dedicated value returned when unsupported command was received.
    public static final EntityV1 UKNOWN_ENTITY_TO_BE_IGNORED = EntityV1.newBuilder()
            .setUuid("dummy")
            .setEntryDate(System.currentTimeMillis())
            .setEntityTypeName("dummy")
            .setEntitySubdomainName("dummy")
            .setEntityIdInSubdomain("UKNOWN_ENTITY_TO_BE_IGNORED")
            .build();
    // Dedicated value returned when a create command was received for already existing entity.
    public static final EntityV1 ALREADY_EXISTING_ENTITY_TO_BE_IGNORED = EntityV1.newBuilder()
            .setUuid("dummy")
            .setEntryDate(System.currentTimeMillis())
            .setEntityTypeName("dummy")
            .setEntitySubdomainName("dummy")
            .setEntityIdInSubdomain("ALREADY_EXISTING_ENTITY_TO_BE_IGNORED")
            .build();
    // Dedicated value returned when a delete command was received for not existing entity.
    public static final EntityV1 NOT_EXISTING_ENTITY_TO_BE_IGNORED = EntityV1.newBuilder()
            .setUuid("dummy")
            .setEntryDate(System.currentTimeMillis())
            .setEntityTypeName("dummy")
            .setEntitySubdomainName("dummy")
            .setEntityIdInSubdomain("NOT_EXISTING_ENTITY_TO_BE_IGNORED")
            .build();

    public static boolean checkIfEntityShouldBeIgnored(EntityV1 entity) {

        return entity == UKNOWN_ENTITY_TO_BE_IGNORED ||
                entity == ALREADY_EXISTING_ENTITY_TO_BE_IGNORED ||
                entity == NOT_EXISTING_ENTITY_TO_BE_IGNORED;
    }
}