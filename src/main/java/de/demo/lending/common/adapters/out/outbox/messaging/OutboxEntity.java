package de.demo.lending.common.adapters.out.outbox.messaging;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

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

    @Column(nullable = false)
    private String aggregate_type;        // loan, inventory, payment

    @Column(nullable = false)
    private String type;                  // z. B. "loan.requested.v1"

    @Column(columnDefinition = "text", nullable = false)
    private String payload;               // JSON String (Event-Payload)

    @Column(columnDefinition = "text")
    private String headers;               // optional: Meta (correlationId, etc.)

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant publishedAt;          // null bis gesendet

    @Column(nullable = false)
    private int attempt = 0;

    @Column(columnDefinition = "text")
    private String errorMessage;
}
