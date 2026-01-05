package de.demo.lending.common.valueobjects;

import java.util.UUID;

/**
 * Value Object für die Identität einer Kopie.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public record CopyId(UUID value) {

    public CopyId {
        if (value == null) {
            throw new IllegalArgumentException("CopyId darf nicht null sein");
        }
    }

    public static CopyId newId() {
        return new CopyId(UUID.randomUUID());
    }

    public static CopyId of(UUID uuid) {
        return new CopyId(uuid);
    }

    public static CopyId of(String uuidString) {
        return new CopyId(UUID.fromString(uuidString));
    }
}