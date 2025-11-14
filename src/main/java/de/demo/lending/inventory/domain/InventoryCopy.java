package de.demo.lending.inventory.domain;

import java.time.Instant;
import java.util.UUID;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.event.BookReserved;
import de.demo.lending.inventory.domain.event.ReservationCreated;
import de.demo.lending.loan.domain.LoanId;

public class InventoryCopy extends AggregateRoot {
    public enum InventoryState {IN_TRANSIENT, AVAILABLE, RESERVED, LOANED}

    private final BookId bookId;
    private final UserId userId;
    private InventoryState state;
    private Instant updatedAt;
    private final String bookTitle;
    private final LoanId loanId;
    private final ReservationId reservationId;

    private final java.util.List<Object> domainEvents = new java.util.ArrayList<>();

    public InventoryCopy(String id, LoanId loanId, String correlationId, BookId bookId, UserId userId,
                         String bookTitle, Instant updatedAt, ReservationId reservationId) {
        super(id, correlationId);

        this.bookId = bookId;
        this.userId = userId;
        this.loanId = loanId;
        this.updatedAt = updatedAt;
        this.bookTitle = bookTitle;
        this.reservationId = reservationId;
    }

    public static InventoryCopy createNew(String correlationId, String causationId, LoanId loanId,
                                          BookId bookId, String bookTitle, UserId userId) {
        var now = Instant.now();
        InventoryCopy newInventory = new InventoryCopy(CopyId.newId().toString(), loanId, correlationId, bookId, userId,
                bookTitle, now, ReservationId.newId());
        newInventory.state = InventoryState.IN_TRANSIENT;
        newInventory.raise(new ReservationCreated(loanId, correlationId, causationId, ReservationId.newId(), bookTitle, userId, Instant.now()));
        return newInventory;
    }

    public CopyId getCopyId() {
        UUID uuid = UUID.fromString(super.getId());
        return CopyId.of(uuid);
    }

    public BookId getBookId() {
        return bookId;
    }

    public InventoryState getState() {
        return state;
    }

    public void reserve(String correlationId, String causationId) {
        if (this.state != InventoryState.IN_TRANSIENT && this.state != InventoryState.AVAILABLE)
            throw new IllegalStateException("Copy not AVAILABLE");
        this.state = InventoryState.RESERVED;
        this.updatedAt = Instant.now();

        raise(new BookReserved(this.loanId, correlationId, causationId, getCopyId(), this.bookTitle, this.userId, this.bookId, this.reservationId));
    }

    /**
     * Wird aufgerufen, wenn das Buch phyisch ankommt
     */
    public void markAsAvailable() {
        if (this.state != InventoryState.IN_TRANSIENT) throw new IllegalStateException("Copy not ordered!");
        this.state = InventoryState.AVAILABLE;
        this.updatedAt = Instant.now();

        // BookArrived Event senden
        //raise(new BookReserved(loanId, correlationId, causationId, newInventory.getCopyId(), bookTitle, userId, bookId, newInventory.reservationId));
    }

    public UserId getUserId() {
        return userId;
    }

    public String getBookTitle() {
        return bookTitle;
    }
}