package de.demo.lending.procurement.domain;

import static java.util.Objects.requireNonNull;

import java.time.Instant;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.procurement.domain.event.BookOrderedExternally;
import de.demo.lending.procurement.domain.event.BookReceived;
import de.demo.lending.procurement.domain.event.ProcurementInitiated;

/**
 * Aggregate Root - Procurement Order
 * Reine Domain-Logik, KEINE Infrastructure-Abhängigkeiten!
 */
public class ProcurementOrder extends AggregateRoot {

    public enum ProcurementStatus {
        INITIATED,          // Procurement wurde gestartet
        ORDERED,            // Bei externer Library bestellt
        IN_TRANSIT,         // Buch ist unterwegs (optional)
        RECEIVED,           // Buch ist angekommen
        COMPLETED,          // Procurement abgeschlossen (Buch im Inventory)
        CANCELLED           // Storniert (z.B. nicht verfügbar)
    }

    private final LoanId loanId;
    private UserId userId;
    //private final BookTitle bookTitle;

    private ExternalLibraryId externalLibraryId;
    private BookId bookId;
    private Isbn isbn;

    private ProcurementStatus status;

    private Instant orderedAt;
    private long estimatedArrival;
    private Instant receivedAt;


    // Private constructor - nur via Factory Methods
    private ProcurementOrder(
            ProcurementOrderId id,
            LoanId loanId,
            BookId bookId,
            Isbn isbn
    ) {
        super(id.value(), "");
        this.loanId = requireNonNull(loanId);
        this.isbn = requireNonNull(isbn);
        this.bookId = requireNonNull(bookId);
        this.status = ProcurementStatus.INITIATED;
        this.orderedAt = Instant.now();
        this.estimatedArrival = 0L;
    }

    // === Factory Methods ===
    public static ProcurementOrder create(ProcurementOrderId id, LoanId loanId,
                                          ExternalLibraryId externalLibraryId, BookId bookId, Isbn isbn, ProcurementStatus status,
                                          Instant orderedAt, long estimatedArrival, Instant receivedAt, String receivedBy) {

        ProcurementOrder order = new ProcurementOrder(id, loanId, bookId, isbn);
        order.externalLibraryId = externalLibraryId;
        order.bookId = bookId;
        order.isbn = isbn;
        order.status = status;
        order.orderedAt = orderedAt;
        order.estimatedArrival = estimatedArrival;
        order.receivedAt = receivedAt;
        return order;
    }

    public static ProcurementOrder initiate(
            LoanId loanId,
            BookId bookId,
            UserId userId,
            Isbn isbn
            //ExternalLibraryId externalLibraryId,

    ) {
        ProcurementOrder order = new ProcurementOrder(
                ProcurementOrderId.newId(),
                loanId,
                bookId,
                isbn
        );
        order.userId = userId;

        order.raise(ProcurementInitiated.of(
                ProcurementOrderId.of(order.getId()),
                order.bookId,
                order.isbn,
                order.loanId,
                order.userId
        ));

        return order;
    }

    // === Business Methods ===

    public void confirmExternalOrder(
            BookId bookId,
            Isbn isbn,
            long estimatedArrival
    ) {
        if (this.status != ProcurementStatus.INITIATED) {
            throw new IllegalStateException(
                    "Can only confirm order in INITIATED state, current: " + this.status
            );
        }

        this.isbn = requireNonNull(isbn);
        this.bookId = requireNonNull(bookId);
        this.estimatedArrival = estimatedArrival;
        this.status = ProcurementStatus.ORDERED;

        raise(BookOrderedExternally.of(
                ProcurementOrderId.of(getId()),
                this.loanId,
                this.userId,
                this.estimatedArrival,
                this.bookId,
                this.isbn
        ));
    }

    /*public void markInTransit() {
        if (this.status != ProcurementStatus.ORDERED) {
            throw new IllegalStateException(
                    "Can only mark as in-transit from ORDERED state"
            );
        }

        this.status = ProcurementStatus.IN_TRANSIT;

        registerEvent(new BookInTransitEvent(
                this.id,
                this.loanId,
                this.externalLibraryId
        ));
    }*/

    public void markAsReceived() {
        if (this.status != ProcurementStatus.IN_TRANSIT
                && this.status != ProcurementStatus.ORDERED) {
            throw new IllegalStateException(
                    "Invalid state for receiving: " + this.status
            );
        }

        this.status = ProcurementStatus.RECEIVED;
        this.receivedAt = Instant.now();


        raise(BookReceived.of(getProcurementOrderId(), getLoanId(), getBookId(), getIsbn(), getUserId()));
    }

    /*public void complete() {
        if (this.status != ProcurementStatus.RECEIVED) {
            throw new IllegalStateException(
                    "Can only complete after receiving"
            );
        }

        this.status = ProcurementStatus.COMPLETED;

        registerEvent(new ProcurementCompletedEvent(
                this.id,
                this.loanId
        ));
    }

    public void cancel(String reason) {
        if (this.status == ProcurementStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot cancel completed procurement"
            );
        }

        this.status = ProcurementStatus.CANCELLED;

        registerEvent(new ProcurementCancelledEvent(
                this.id,
                this.loanId,
                reason
        ));
    }*/


    // getter

    public ProcurementOrderId getProcurementOrderId() {
        return ProcurementOrderId.of(getId());
    }

    public LoanId getLoanId() {
        return loanId;
    }

    public UserId getUserId() {
        return userId;
    }

    public ExternalLibraryId getExternalLibraryId() {
        return externalLibraryId;
    }

    public Isbn getIsbn() {
        return this.isbn;
    }

    public BookId getBookId() {
        return bookId;
    }

    public ProcurementStatus getStatus() {
        return status;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public long getEstimatedArrival() {
        return estimatedArrival;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}