package de.demo.lending.procurement.adapters.out.persistence;

import java.time.Instant;
import java.util.UUID;

import de.demo.lending.common.adapters.out.persistence.VersionedEntity;
import de.demo.lending.procurement.domain.ProcurementOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "procurement_orders")
@Getter
@Setter
public class ProcurementOrderEntity extends VersionedEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "loan_id", nullable = false)
    private UUID loanId;

    @Column(name = "book_title", nullable = false)
    private String bookTitle;

    @Column(name = "external_library_id")
    private String externalLibraryId;

    @Column(name = "external_order_id")
    private String externalOrderId;

    @Column(name = "isbn")
    private String isbn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcurementOrder.ProcurementStatus status;

    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @Column(name = "estimated_arrival")
    private long estimatedArrival;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "received_by")
    private String receivedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
