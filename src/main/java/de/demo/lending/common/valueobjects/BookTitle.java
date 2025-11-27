package de.demo.lending.common.valueobjects;

/**
 * Value Object für die Identität einer Kopie.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public final class BookTitle {
    private BookTitle(String value) {
    }

    public static BookTitle of(String value) {
        return new BookTitle(value);
    }
}