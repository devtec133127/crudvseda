package de.demo.lending.common.valueobjects;

import java.util.Objects;

/**
 * Value Object für die Identität einer Kopie.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public final class BookTitle {
    private final String value;

    private BookTitle(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static BookTitle of(String value) {
        return new BookTitle(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}