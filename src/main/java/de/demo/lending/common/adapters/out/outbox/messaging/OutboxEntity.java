package de.demo.lending.common.adapters.out.outbox.messaging;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "outbox")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL in Postgres
    private Long id;

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
}
