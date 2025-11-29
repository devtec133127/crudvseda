package de.demo.lending.inventory.domain;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.event.BookRegistered;
import de.demo.lending.inventory.domain.event.BookReserved;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;
import java.util.UUID;

public class InventoryCopy extends AggregateRoot {
    public enum InventoryState {IN_TRANSIENT, NOT_LOCALLY_AVAILABLE, REGISTERED, AVAILABLE, RESERVED, LOANED}

    private final BookId bookId;
    private final UserId userId;
    private InventoryState state;
    private Instant updatedAt;
    private final BookTitle bookTitle;
    private final LoanId loanId;
    private final ReservationId reservationId;
    private long dueDate;

    private final java.util.List<Object> domainEvents = new java.util.ArrayList<>();

    public InventoryCopy(UUID id, LoanId loanId, String correlationId, BookId bookId, UserId userId,
                         BookTitle bookTitle, Instant updatedAt, ReservationId reservationId) {
        super(id, correlationId);

        this.bookId = bookId;
        this.userId = userId;
        this.loanId = loanId;
        this.updatedAt = updatedAt;
        this.bookTitle = bookTitle;
        this.reservationId = reservationId;
    }

    public static InventoryCopy createNew(String correlationId, String causationId, LoanId loanId,
                                          BookId bookId, BookTitle bookTitle, UserId userId) {
        var now = Instant.now();
        InventoryCopy newInventory = new InventoryCopy(CopyId.newId().value(), loanId, correlationId, bookId, userId,
                bookTitle, now, ReservationId.newId());
        newInventory.state = InventoryState.NOT_LOCALLY_AVAILABLE;
        // Hier könnten wir ein technisches Event erstellen, aber kein Domain Event !!!
        //newInventory.raise(new ProcurementRequestedEvent(loanId, correlationId, causationId, true, bookTitle, userId));
        return newInventory;
    }

    public static InventoryCopy createNew(String correlationId, String causationId, LoanId loanId,
                                          BookId bookId, BookTitle bookTitle) {
        return createNew(correlationId, causationId, loanId, bookId, bookTitle, null);
    }

    public CopyId getCopyId() {
        return CopyId.of(getId());
    }

    public BookId getBookId() {
        return bookId;
    }

    public LoanId getLoanId() {
        return loanId;
    }

    public InventoryState getState() {
        return state;
    }

    public void reserve(String correlationId, String causationId, long dueDate) {
        if (this.state != InventoryState.IN_TRANSIENT && this.state != InventoryState.AVAILABLE)
            throw new IllegalStateException("Copy not AVAILABLE");
        this.state = InventoryState.RESERVED;
        this.dueDate = dueDate;
        this.updatedAt = Instant.now();

        raise(new BookReserved(this.loanId, correlationId, causationId, getCopyId(), this.bookTitle, this.userId, this.bookId, this.reservationId));
    }

    /**
     * Wird aufgerufen, wenn das Buch phyisch ankommt
     */
    public void registerBook() {
        if (this.state != InventoryState.IN_TRANSIENT && this.state != InventoryState.NOT_LOCALLY_AVAILABLE)
            throw new IllegalStateException("Copy not ordered!");
        this.state = InventoryState.REGISTERED;
        this.updatedAt = Instant.now();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // BookArrived Event senden
        raise(new BookRegistered(loanId, "", "", getCopyId(), getBookId(), ReservationId.newId()));
    }

    /**
     * Wird aufgerufen, wenn das Buch erfasst wurde
     */
    public void markAsAvailable() {
        if (this.state != InventoryState.REGISTERED) throw new IllegalStateException("Copy not ordered!");
        this.state = InventoryState.AVAILABLE;
        this.updatedAt = Instant.now();

        // BookArrived Event senden
        raise(new BookReserved(loanId, "", "", getCopyId(), getBookTitle(), getUserId(), getBookId(), ReservationId.newId()));
    }

    public UserId getUserId() {
        return userId;
    }

    public BookTitle getBookTitle() {
        return bookTitle;
    }

    public ReservationId getReservationId() {
        return reservationId;
    }

    public long getDueDate() {
        return dueDate;
    }
}