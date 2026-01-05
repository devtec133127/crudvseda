package de.demo.lending.procurement.domain;

import java.util.UUID;

public record ExternalLibraryId(UUID value) {
    public ExternalLibraryId {
        if (value == null) {
            throw new IllegalArgumentException("ExternalLibraryId darf nicht null sein");
        }
    }

    public static ExternalLibraryId newId() {
        return new ExternalLibraryId(UUID.randomUUID());
    }

    public static ExternalLibraryId of(UUID value) {
        return new ExternalLibraryId(value);
    }
}
