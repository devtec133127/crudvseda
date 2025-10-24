package de.demo.lending.payment.domain;

import de.demo.lending.common.valueobjects.UuidId;

import java.util.UUID;

public class PaymentId extends UuidId {
    private PaymentId(UUID value) {
        super(value);
    }

    public static PaymentId newId() {
        return new PaymentId(UUID.randomUUID());
    }

    public static PaymentId of(UUID value) {
        return new PaymentId(value);
    }
}
