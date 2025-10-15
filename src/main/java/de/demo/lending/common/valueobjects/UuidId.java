package de.demo.lending.common.valueobjects;

import java.util.Objects;
import java.util.UUID;

/** Basisklasse für einfache ID-Wrapper (UUID-basiert). */
public abstract class UuidId {
  protected final UUID value;

  protected UuidId(UUID value) {
    this.value = Objects.requireNonNull(value);
  }

  public UUID value() { return value; }

  @Override public String toString() { return value.toString(); }
  @Override public int hashCode() { return value.hashCode(); }
  @Override public boolean equals(Object o) {
    return o != null && this.getClass().equals(o.getClass()) &&
           value.equals(((UuidId)o).value);
  }
}
