package de.demo.lending.inventory.domain;

import java.time.Instant;
import java.util.UUID;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.adapters.out.persistence.InventoryCopyEntity;
import de.demo.lending.inventory.domain.event.BookReserved;
import de.demo.lending.loan.domain.Loan;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.loan.domain.event.LoanRequested;

public class InventoryCopy extends AggregateRoot {
    public enum InventoryState { AVAILABLE, RESERVED, LOANED }

    private final BookId bookId;
    private final UserId userId;
    private InventoryState state;
    private Instant updatedAt;
    private final String bookTitle;

    private final java.util.List<Object> domainEvents = new java.util.ArrayList<>();

    public InventoryCopy(String id, String correlationId, BookId bookId, UserId userId,
                         String bookTitle, Instant updatedAt) {
        super(id, correlationId);

        this.bookId = bookId;
        this.userId = userId;
        this.updatedAt = updatedAt;
        this.bookTitle = bookTitle;
    }

    public static InventoryCopy createNew(String correlationId, String causationId, LoanId loanId,
                                          BookId bookId, String bookTitle, UserId userId) {
        var now = Instant.now();
        InventoryCopy newInventory = new InventoryCopy(CopyId.newId().toString(), correlationId, bookId, userId,
                                                            bookTitle, now);
        newInventory.state = InventoryState.RESERVED;
        newInventory.raise(new BookReserved(loanId, correlationId, causationId, newInventory.getCopyId(), bookTitle, userId, bookId));
        return newInventory;
    }

    private void raise(Object event) {
        domainEvents.add(event);
    }

    public java.util.List<Object> pullDomainEvents() {
        var copy = java.util.List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public CopyId getCopyId() {
        UUID uuid = UUID.fromString(super.getId());
        return CopyId.of(uuid);
    }
    public BookId getBookId() { return bookId; }
    public InventoryState getState() { return state; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void reserve() {
        if (this.state != InventoryState.AVAILABLE) throw new IllegalStateException("Copy not AVAILABLE");
        this.state = InventoryState.RESERVED;
        this.updatedAt = Instant.now();
    }

    public void checkout() {
        if (this.state != InventoryState.RESERVED) throw new IllegalStateException("Copy not RESERVED");
        this.state = InventoryState.LOANED;
        this.updatedAt = Instant.now();
    }

    public void makeAvailable() {
        this.state = InventoryState.AVAILABLE;
        this.updatedAt = Instant.now();
    }

    public UserId getUserId() {
        return userId;
    }

    public String getBookTitle() {
        return bookTitle;
    }
}