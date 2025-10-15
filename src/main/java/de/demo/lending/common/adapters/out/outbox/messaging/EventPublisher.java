package de.demo.lending.common.adapters.out.outbox.messaging;

public interface EventPublisher {
    void enqueue(String type, Object payload);
}
