package de.demo.lending.common.valueobjects;

import java.util.UUID;

/**
 * Value Object für die Identität eines Users.
 * Wird als Wrapper um UUID verwendet, um Typsicherheit zu erhöhen.
 */
public final class UserId extends UuidId {
    private UserId(UUID value) { super(value); }
    public static UserId of(UUID value) { return new UserId(value); }
}