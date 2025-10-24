package de.demo.lending.payment.domain;

import java.util.Optional;

public enum PaymentMethod {
    CREDIT_CARD,
    PAYPAL,
    BANK_TRANSFER;

    public static Optional<PaymentMethod> fromString(String value) {
        try {
            return Optional.of(PaymentMethod.valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}