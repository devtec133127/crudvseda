package de.demo.lending.common.valueobjects;

import de.demo.lending.loan.domain.LoanId;

import java.util.UUID;

/**
 * Value Object für die Identität einer Kopie.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public final class CopyId extends UuidId {
    private CopyId(UUID value) { super(value); }
    public static CopyId newId() { return new CopyId(UUID.randomUUID()); }
    public static CopyId of(UUID value) { return new CopyId(value); }
}