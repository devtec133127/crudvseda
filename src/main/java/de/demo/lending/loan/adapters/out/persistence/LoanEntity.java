package de.demo.lending.loan.adapters.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class LoanEntity {
    @Id private UUID id;
    @Column(nullable=false) private UUID userId;
    @Column(nullable=false) private UUID bookId;
    private UUID copyId;
    @Column(nullable=false) private String status;
    private LocalDate dueDate;
    private Instant createdAt;
    private Instant updatedAt;


}