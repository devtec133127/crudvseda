package de.demo.lending.procurement.domain;

import de.demo.lending.common.valueobjects.UuidId;

import java.util.UUID;

public class ExternalLibraryId extends UuidId {
    protected ExternalLibraryId(UUID value) {
        super(value);
    }

    public static ExternalLibraryId newId() {
        return new ExternalLibraryId(UUID.randomUUID());
    }

    public static ExternalLibraryId of(UUID value) {
        return new ExternalLibraryId(value);
    }
}
