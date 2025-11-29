package de.demo.lending.inventory.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String bookId;

    @Column(nullable = false)
    private String userId;

    private String bookTitle;

    private String copyId;

    @Column(nullable = false)
    private String state; // AVAILABLE, RESERVED, CHECKED_OUT

    private String location;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;
}
