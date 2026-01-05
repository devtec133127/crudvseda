package de.demo.lending.loan.adapters.out.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class LoanEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID loanId;
    @Column(nullable = false)
    private UUID userId;
    @Column(nullable = false)
    private String isbn;
    private UUID copyId;
    @Column(nullable = false)
    private String status;
    private LocalDate dueDate;
    private Instant createdAt;
    private Instant updatedAt;


}