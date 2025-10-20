package de.demo.lending.inventory.adapters.out.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_copy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryCopyEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String bookId;

    @Column(nullable = false)
    private String state; // AVAILABLE, RESERVED, CHECKED_OUT

    private String location;
    private Instant createdAt;
    private Instant updatedAt;
}