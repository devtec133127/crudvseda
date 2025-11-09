package de.demo.lending.read.adapters.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Entity
@Table(name = "loan_status_read")
@Getter
@Service
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
