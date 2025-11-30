package de.demo.lending.procurement.adapters.out.persistence;

import de.demo.lending.common.adapters.out.persistence.VersionedEntity;
import de.demo.lending.procurement.domain.ProcurementOrder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

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

    @Column(name = "external_library_id")
    private String externalLibraryId;

    @Column(name = "external_order_id")
    private String externalOrderId;

    @Column(name = "isbn", nullable = false)
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
