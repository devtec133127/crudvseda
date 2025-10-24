package de.demo.lending.payment.domain;

import java.util.Map;

public class PaymentMethod {
    private String type;
    private String provider;
    private Map<String, String> metadata;

    public PaymentMethod(String type) {
        this.type = type;
    }
}
