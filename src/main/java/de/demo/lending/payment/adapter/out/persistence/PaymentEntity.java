package de.demo.lending.payment.adapter.out.persistence;

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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id
    private UUID id;

    @Column(name = "loan_id", nullable = false)
    private String loanId;

    @Column(name = "book_id", nullable = false)
    private String bookId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(nullable = false)
    private String state;

    @Column(name = "payment_method")
    private String paymentMethodJson; // oder JSONB, oder separate Felder

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}

