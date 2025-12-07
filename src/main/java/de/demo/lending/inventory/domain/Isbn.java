package de.demo.lending.inventory.domain;

public class Isbn {
    private final String value;

    private Isbn(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Isbn of(String value) {
        return new Isbn(value);
    }
}