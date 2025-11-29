package de.demo.lending.inventory.domain;

import de.demo.lending.common.valueobjects.UuidId;

import java.util.UUID;

/**
 * Value Object für die Identität einer Ausleihe.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public final class PendingReservationId extends UuidId {
    private PendingReservationId(UUID value) {
        super(value);
    }

    public static PendingReservationId newId() {
        return new PendingReservationId(UUID.randomUUID());
    }

    public static PendingReservationId of(UUID value) {
        return new PendingReservationId(value);
    }
}