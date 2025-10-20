package de.demo.lending.common.valueobjects;

import org.springframework.data.util.Pair;

import java.util.UUID;

/**
 * Value Object für die Identität eines Buches.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public final class BookId {
    private final String value;

    private BookId(String value) { this.value = value; }
    public String value() { return value; }
    public static BookId of(String value) { return new BookId(value); }
}