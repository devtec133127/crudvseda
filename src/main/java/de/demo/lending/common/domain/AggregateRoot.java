package de.demo.lending.common.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.demo.lending.common.domain.events.BaseDomainEvent;

/**
 * Basis für echte DDD-Aggregate (kein JPA). Hält gemeinsame Metadaten + event-bag.
 */
public abstract class AggregateRoot {
    private final UUID id;                       // aggregate id (z.B. reservationId, copyId)
    private final String correlationId;            // meist requestId
    private final Instant createdAt;
    private UUID lastProcessedEventId;           // optional, für idempotenz/audit

    // transient: während Lebenszyklus des Aggregats gesammelte Domain-Events
    private final List<BaseDomainEvent> producedEvents = new ArrayList<>();

    protected AggregateRoot(UUID id, String correlationId) {
        this.id = (id == null) ? UUID.randomUUID() : id;
        this.correlationId = correlationId;
        this.createdAt = Instant.now();
    }

    // --- getters ---
    public UUID getId() {
        return id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getLastProcessedEventId() {
        return lastProcessedEventId;
    }

    // --- produced events handling ---
    protected void raise(BaseDomainEvent evt) {
        Objects.requireNonNull(evt, "event must not be null");
        producedEvents.add(evt);
        // optional: track last produced event id for idempotenz/audit
        if (evt.getEventId() != null) {
            this.lastProcessedEventId = evt.getEventId();
        }
    }

    /**
     * Liefert und leert die gesammelten Domain-Events (für Outbox / Publisher).
     */
    public List<BaseDomainEvent> pullProducedEvents() {
        List<BaseDomainEvent> copy = new ArrayList<>(producedEvents);
        // wichtig
        producedEvents.clear();
        return Collections.unmodifiableList(copy);
    }

    // helper: add already built event (z.B. created in factory)
    protected void addProducedEvent(BaseDomainEvent evt) {
        raise(evt);
    }

    // equals/hashCode auf id-Basis (Aggregat-Identität)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AggregateRoot)) return false;
        AggregateRoot that = (AggregateRoot) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}