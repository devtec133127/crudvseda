package de.demo.lending.common.adapters.out.outbox.messaging;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL in Postgres
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false, length = 128)
    private String aggregate_type;        // loan, inventory, payment

    @Column(nullable = false, length = 128)
    private String type;                  // z. B. "loan.requested.v1"

    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;               // JSON String (Event-Payload)

    @Column(columnDefinition = "jsonb")
    private String headers;               // optional: Meta (correlationId, etc.)

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant publishedAt;          // null bis gesendet

    @Column(nullable = false)
    private int attempt = 0;

    @Column
    private String errorMessage;
}
