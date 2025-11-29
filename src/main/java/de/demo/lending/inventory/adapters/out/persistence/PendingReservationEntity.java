package de.demo.lending.inventory.adapters.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pending_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PendingReservationEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String bookTitle;  // ⭐ Lookup-Key!

    @Column(nullable = false)
    private UUID loanId;

    @Column(nullable = false)
    private UUID userId; // stored as UUID string

    private long dueDate; // changed from LocalDate to long (epoch days/ms as chosen)

    private Instant createdAt;
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