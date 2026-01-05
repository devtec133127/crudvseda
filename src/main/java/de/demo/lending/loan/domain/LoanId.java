package de.demo.lending.loan.domain;

import java.util.UUID;

/**
 * Value Object für die Identität einer Ausleihe.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public record LoanId(UUID value) {

    public LoanId {
        if (value == null) {
            throw new IllegalArgumentException("LoanId darf nicht null sein");
        }
    }

    public static LoanId newId() {
        return new LoanId(UUID.randomUUID());
    }

    public static LoanId of(UUID uuid) {
        return new LoanId(uuid);
    }

    public static LoanId of(String uuidString) {
        return new LoanId(UUID.fromString(uuidString));
    }
}