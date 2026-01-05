package de.demo.lending.common.valueobjects;

/**
 * Value Object für die Identität eines Buches.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public record BookId(String value) {

    public BookId {
        if (value == null) {
            throw new IllegalArgumentException("BookId darf nicht null sein");
        }
    }

    public static BookId of(String value) {
        return new BookId(value);
    }
}