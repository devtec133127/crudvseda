package de.demo.lending.payment.domain;

import java.util.Objects;

public class Money {

    private long cents;
    private String currency;

    public Money(long cents, String currency) {
        if (cents < 0) throw new IllegalArgumentException("Amount must be >= 0");
        this.cents = cents;
        this.currency = Objects.requireNonNull(currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.cents + other.cents, currency);
    }

    public static Money zero() {
        return new Money(0, "EUR");
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) throw new IllegalArgumentException("Currency mismatch");
    }

    public long getCents() {
        return cents;
    }

    public String getCurrency() {
        return currency;
    }
}
