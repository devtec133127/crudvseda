package de.demo.lending.read.adapters.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loan_status_read")
@Getter
public class LoanStatusRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL in Postgres
    private Long id;

    @Column(nullable = false)
    private UUID loanId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private String loanStatus;

    @Column(nullable = false)
    private String paymentStatus;

    private BigDecimal fee;

    private Instant paymentPaidAt;

    private Instant updatedAt;
}
