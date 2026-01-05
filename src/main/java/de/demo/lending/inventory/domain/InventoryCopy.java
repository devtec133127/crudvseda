package de.demo.lending.inventory.domain;

import java.time.Instant;
import java.util.UUID;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.events.BookReserved;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.event.BookRegistered;

public class InventoryCopy extends AggregateRoot {
    public enum InventoryState {IN_TRANSIENT, NOT_LOCALLY_AVAILABLE, REGISTERED, AVAILABLE, RESERVED, LOANED}

    private final BookId bookId;
    private final Isbn isbn;
    private InventoryState state;
    private Instant updatedAt;
    private final LoanId loanId;
    private final ReservationId reservationId;
    private long dueDate;

    private final java.util.List<Object> domainEvents = new java.util.ArrayList<>();

    public InventoryCopy(UUID id, LoanId loanId, String correlationId, BookId bookId, Isbn isbn,
                         Instant updatedAt, ReservationId reservationId) {
        super(id, correlationId);

        this.bookId = bookId;
        this.isbn = isbn;
        this.loanId = loanId;
        this.updatedAt = updatedAt;
        this.reservationId = reservationId;
    }

    public static InventoryCopy createNew(String correlationId, String causationId, LoanId loanId,
                                          BookId bookId, Isbn isbn, UserId userId) {
        var now = Instant.now();
        InventoryCopy newInventory = new InventoryCopy(CopyId.newId().value(), loanId, correlationId, bookId, isbn,
                now, ReservationId.newId());
        newInventory.state = InventoryState.NOT_LOCALLY_AVAILABLE;
        // Hier könnten wir ein technisches Event erstellen, aber kein Domain Event !!!
        //newInventory.raise(new ProcurementRequestedEvent(loanId, correlationId, causationId, true, bookTitle, userId));
        return newInventory;
    }

    public static InventoryCopy createNew(String correlationId, String causationId, LoanId loanId,
                                          BookId bookId, Isbn isbn) {
        return createNew(correlationId, causationId, loanId, bookId, isbn, null);
    }

    public CopyId getCopyId() {
        return CopyId.of(getId());
    }

    public BookId getBookId() {
        return bookId;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    public LoanId getLoanId() {
        return loanId;
    }

    public InventoryState getState() {
        return state;
    }

    public void reserve(String correlationId, String causationId, long dueDate, UserId userId) {
        if (this.state != InventoryState.IN_TRANSIENT && this.state != InventoryState.AVAILABLE)
            throw new IllegalStateException("Copy not AVAILABLE");
        this.state = InventoryState.RESERVED;
        this.dueDate = dueDate;
        this.updatedAt = Instant.now();

        raise(new BookReserved(this.loanId, correlationId, causationId, getCopyId(), userId, this.bookId, this.reservationId));
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
        //raise(new BookRegistered(loanId, "", "", getCopyId(), getBookId(), ReservationId.newId()));
    }

    /**
     * Wird aufgerufen, wenn das Buch erfasst wurde
     */
    public void markAsAvailable() {
        if (this.state != InventoryState.REGISTERED) throw new IllegalStateException("Copy not ordered!");
        this.state = InventoryState.AVAILABLE;
        this.updatedAt = Instant.now();

        // BookArrived Event senden
        //raise(new BookReserved(loanId, "", "", getCopyId(), getBookTitle(), getUserId(), getBookId(), ReservationId.newId()));
        raise(new BookRegistered(loanId, "", "", getCopyId(), getBookId(), ReservationId.newId()));
    }

    public ReservationId getReservationId() {
        return reservationId;
    }

    public long getDueDate() {
        return dueDate;
    }
}