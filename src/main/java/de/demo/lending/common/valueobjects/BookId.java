package de.demo.lending.common.valueobjects;

import java.util.UUID;

/**
 * Value Object für die Identität eines Buches.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public final class BookId extends UuidId {
    private BookId(UUID value) { super(value); }
    public static BookId of(UUID value) { return new BookId(value); }
}