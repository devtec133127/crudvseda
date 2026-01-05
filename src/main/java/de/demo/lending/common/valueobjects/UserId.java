package de.demo.lending.common.valueobjects;

import java.util.UUID;

/**
 * Value Object für die Identität eines Users.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId darf nicht null sein");
        }
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId of(String uuidString) {
        return new UserId(UUID.fromString(uuidString));
    }
}