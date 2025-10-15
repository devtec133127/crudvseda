package de.demo.lending.loan.domain;

import java.util.UUID;

import de.demo.lending.common.valueobjects.UuidId;

/**
 * Value Object für die Identität einer Ausleihe.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public final class LoanId extends UuidId {
    private LoanId(UUID value) { super(value); }
    public static LoanId newId() { return new LoanId(UUID.randomUUID()); }
    public static LoanId of(UUID value) { return new LoanId(value); }
}