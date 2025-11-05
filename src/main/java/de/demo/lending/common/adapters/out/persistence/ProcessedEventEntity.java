package de.demo.lending.common.adapters.out.persistence;

import java.time.Instant;

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
@Table(name = "processed_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEventEntity {
    @Id
    private String eventId;        // aus Payload/eventId header
    @Column(nullable = false)
    private String consumer;       // e.g., "inventory"
    @Column(nullable = false)
    private Instant receivedAt;
}
