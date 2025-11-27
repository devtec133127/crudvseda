package de.demo.lending.procurement.adapters.out.persistence;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.loan.domain.LoanId;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "procurement_orders")
public class ProcurementOrder {

    @EmbeddedId
    private ProcurementOrderId id;

    // Verknüpfung zum Loan Context
    @Embedded
    private LoanId loanId;

    // Buch-Informationen
    @Embedded
    private BookTitle bookTitle;

    @Embedded
    private BookId isbn;

    // Externe Library Info
    //@Embedded
    //private ExternalLibraryId externalLibraryId;

    private String externalOrderId;  // ID im externen System

    // Status-Tracking
    @Enumerated(EnumType.STRING)
    private ProcurementStatus status;

    // Zeitstempel
    private Instant orderedAt;
    private Instant estimatedArrival;
    private Instant receivedAt;

    // Lieferdetails (wenn angekommen)
    @Embedded
    private ShelfLocation targetLocation;

    @Enumerated(EnumType.STRING)
    private PhysicalCondition physicalCondition;

    private String receivedBy;  // Mitarbeiter-ID

    // Domain Events
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    // === Business Methods ===

    public static ProcurementOrder initiate(
            LoanId loanId,
            BookTitle bookTitle,
            ExternalLibraryId externalLibraryId
    ) {
        ProcurementOrder order = new ProcurementOrder();
        order.id = ProcurementOrderId.generate();
        order.loanId = loanId;
        order.bookTitle = bookTitle;
        order.externalLibraryId = externalLibraryId;
        order.status = ProcurementStatus.INITIATED;
        order.orderedAt = Instant.now();

        order.registerEvent(new ProcurementInitiatedEvent(
                order.id,
                order.loanId,
                order.bookTitle
        ));

        return order;
    }

    public void confirmExternalOrder(
            String externalOrderId,
            Isbn isbn,
            Instant estimatedArrival
    ) {
        if (this.status != ProcurementStatus.INITIATED) {
            throw new IllegalStateException(
                    "Can only confirm order in INITIATED state"
            );
        }

        this.externalOrderId = externalOrderId;
        this.isbn = isbn;
        this.estimatedArrival = estimatedArrival;
        this.status = ProcurementStatus.ORDERED;

        registerEvent(new BookOrderedExternallyEvent(
                this.id,
                this.loanId,
                this.externalOrderId,
                this.externalLibraryId,
                this.estimatedArrival
        ));
    }

    public void markInTransit() {
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
        this.targetLocation = location;
        this.physicalCondition = condition;
        this.receivedBy = receivedByStaff;

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
    }

    // Event handling
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
