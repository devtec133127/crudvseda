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
