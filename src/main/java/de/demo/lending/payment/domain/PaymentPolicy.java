package de.demo.lending.payment.domain;

public class PaymentPolicy {
    public static final Money STANDARD_FEE = new Money(5L, "Euro");

    private PaymentPolicy() {
    }
}
