package de.demo.lending.inventory.domain;

import java.util.UUID;

/**
 * Value Object für die Identität einer Ausleihe.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public record ReservationId(UUID value) {
    public ReservationId {
        if (value == null) {
            throw new IllegalArgumentException("ReservationId darf nicht null sein");
        }
    }

    public static ReservationId newId() {
        return new ReservationId(UUID.randomUUID());
    }

    public static ReservationId of(UUID value) {
        return new ReservationId(value);
    }
}