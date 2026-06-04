package de.demo.lending.catalog.domain;

import java.util.UUID;

public record CatalogBookId(UUID value) {

    public CatalogBookId {
        if (value == null) {
            throw new IllegalArgumentException("CatalogBookId darf nicht null sein");
        }
    }

    public static CatalogBookId newId() {
        return new CatalogBookId(UUID.randomUUID());
    }

    public static CatalogBookId of(UUID value) {
        return new CatalogBookId(value);
    }
}
