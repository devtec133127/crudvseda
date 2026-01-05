package de.demo.lending.inventory.domain;

import java.util.UUID;

/**
 * Value Object für die Identität einer Ausleihe.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public record PendingReservationId(UUID value) {
    public PendingReservationId {
        if (value == null) {
            throw new IllegalArgumentException("PendingReservationId darf nicht null sein");
        }
    }

    public static PendingReservationId newId() {
        return new PendingReservationId(UUID.randomUUID());
    }

    public static PendingReservationId of(UUID value) {
        return new PendingReservationId(value);
    }

    public static PendingReservationId of(String uuidString) {
        return new PendingReservationId(UUID.fromString(uuidString));
    }
}