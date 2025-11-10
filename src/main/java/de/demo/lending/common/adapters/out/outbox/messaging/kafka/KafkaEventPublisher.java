package de.demo.lending.common.adapters.out.outbox.messaging.kafka;

import de.demo.lending.common.adapters.out.outbox.messaging.OutboxMarker;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final OutboxMarker marker;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafka, OutboxMarker marker) {
        this.kafka = kafka;
        this.marker = marker;
    }
    
    public void enqueue(long eventId, String type, Object payload) {
        ProducerRecord<String, String> record = new ProducerRecord<>("orders-topic", type, payload.toString());
        CompletableFuture<SendResult<String, String>> future = kafka.send(record);

        // Callback: bei Erfolg -> flag setzen; bei Fehler -> attempt_count++
        future.thenAccept(result -> {

                    //publisher.enqueue(LOAN_REQUESTED_V1, payload);
                    marker.markAsSent(eventId, Instant.now());
                })
                .exceptionally(ex -> {
                    marker.incrementAttempt(eventId, ex.getMessage());
                    return null;
                });
    }
}
