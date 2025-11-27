package de.demo.lending.procurement.domain;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.event.BookOrderedExternally;
import de.demo.lending.procurement.domain.event.ProcurementInitiated;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

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
    private final UserId userId;
    private final BookTitle bookTitle;

    private ExternalLibraryId externalLibraryId;
    private String externalOrderId;
    private BookId isbn;

    private ProcurementStatus status;

    private Instant orderedAt;
    private long estimatedArrival;
    private Instant receivedAt;

    //private ShelfLocation targetLocation;
    //private PhysicalCondition physicalCondition;
    private String receivedBy;

    private final List<BaseDomainEvent> domainEvents = new ArrayList<>();

    // Private constructor - nur via Factory Methods
    private ProcurementOrder(
            ProcurementOrderId id,
            LoanId loanId,
            UserId userId,
            BookTitle bookTitle
    ) {
        super(id.value().toString(), "");
        this.loanId = requireNonNull(loanId);
        this.userId = requireNonNull(userId);
        this.bookTitle = requireNonNull(bookTitle);
        this.status = ProcurementStatus.INITIATED;
    }

    // === Factory Methods ===

    public static ProcurementOrder initiate(
            LoanId loanId,
            UserId userId,
            BookTitle bookTitle
            //ExternalLibraryId externalLibraryId,

    ) {
        ProcurementOrder order = new ProcurementOrder(
                ProcurementOrderId.newId(),
                loanId,
                userId,
                bookTitle
        );

        order.raise(ProcurementInitiated.of(
                ProcurementOrderId.of(UUID.fromString(order.getId())),
                order.bookTitle,
                order.loanId,
                userId
        ));

        return order;
    }

    // === Business Methods ===

    public void confirmExternalOrder(
            String externalOrderId,
            BookId isbn,
            long estimatedArrival
    ) {
        if (this.status != ProcurementStatus.INITIATED) {
            throw new IllegalStateException(
                    "Can only confirm order in INITIATED state, current: " + this.status
            );
        }

        this.externalOrderId = requireNonNull(externalOrderId);
        this.isbn = requireNonNull(isbn);
        this.estimatedArrival = estimatedArrival;
        this.status = ProcurementStatus.ORDERED;

        raise(BookOrderedExternally.of(
                ProcurementOrderId.of(UUID.fromString(getId())),
                this.externalOrderId,
                this.loanId,
                this.userId,
                this.estimatedArrival
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
    }

    public void markAsReceived(
            ShelfLocation location,
            PhysicalCondition condition,
            String receivedByStaff
    ) {
        if (this.status != ProcurementStatus.IN_TRANSIT
                && this.status != ProcurementStatus.ORDERED) {
            throw new IllegalStateException(
                    "Invalid state for receiving: " + this.status
            );
        }

        this.status = ProcurementStatus.RECEIVED;
        this.receivedAt = Instant.now();
        this.targetLocation = requireNonNull(location);
        this.physicalCondition = requireNonNull(condition);
        this.receivedBy = requireNonNull(receivedByStaff);

        registerEvent(new BookReceivedEvent(
                this.id,
                this.loanId,
                this.isbn,
                this.bookTitle,
                this.targetLocation
        ));
    }

    public void complete() {
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

}