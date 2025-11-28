package de.demo.lending.inventory.adapters.out.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_copy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryCopyEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String bookId;

    @Column(nullable = false)
    private String loanId;
    
    private String userId;

    @Column(nullable = false)
    private String reservationId;

    private String bookTitle;

    @Column(nullable = false)
    private String state; // AVAILABLE, RESERVED, CHECKED_OUT

    private String location;
    private Instant createdAt;
    private Instant updatedAt;
}