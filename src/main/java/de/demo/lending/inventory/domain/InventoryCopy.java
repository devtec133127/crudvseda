package de.demo.lending.inventory.domain;

import java.time.Instant;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;

public class InventoryCopy {
    public enum State { AVAILABLE, RESERVED, CHECKED_OUT }

    private final CopyId id;
    private final BookId bookId;
    private State state;
    private final Instant createdAt;
    private Instant updatedAt;
    private String location;

    public InventoryCopy(CopyId id, BookId bookId, State state, Instant createdAt, Instant updatedAt, String location) {
        this.id = id;
        this.bookId = bookId;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.location = location;
    }

    public CopyId getId() { return id; }
    public BookId getBookId() { return bookId; }
    public State getState() { return state; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getLocation() { return location; }

    public void reserve() {
        if (this.state != State.AVAILABLE) throw new IllegalStateException("Copy not AVAILABLE");
        this.state = State.RESERVED;
        this.updatedAt = Instant.now();
    }

    public void checkout() {
        if (this.state != State.RESERVED) throw new IllegalStateException("Copy not RESERVED");
        this.state = State.CHECKED_OUT;
        this.updatedAt = Instant.now();
    }

    public void makeAvailable() {
        this.state = State.AVAILABLE;
        this.updatedAt = Instant.now();
    }
}