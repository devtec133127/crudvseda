package de.demo.lending.common.application.ports.out;

import de.demo.lending.common.application.dto.DtoPayload;

public interface EventPublisher {
    //void enqueue(String type, Object payload);

    void enqueue(String topic, DtoPayload payload);
}
