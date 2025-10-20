package de.demo.lending.inventory.domain;

import de.demo.lending.common.valueobjects.UuidId;

import java.util.UUID;

/**
 * Value Object für die Identität einer Ausleihe.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public final class ReservationId extends UuidId {
    private ReservationId(UUID value) { super(value); }
    public static ReservationId newId() { return new ReservationId(UUID.randomUUID()); }
    public static ReservationId of(UUID value) { return new ReservationId(value); }
}