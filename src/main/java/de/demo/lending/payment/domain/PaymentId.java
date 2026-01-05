package de.demo.lending.payment.domain;

import java.util.UUID;

public record PaymentId(UUID value) {
    public PaymentId {
        if (value == null) {
            throw new IllegalArgumentException("PaymentId darf nicht null sein");
        }
    }

    public static PaymentId newId() {
        return new PaymentId(UUID.randomUUID());
    }

    public static PaymentId of(UUID value) {
        return new PaymentId(value);
    }
}
