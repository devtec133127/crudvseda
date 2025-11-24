package de.demo.lending.common.adapters.out.outbox.messaging.kafka;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.adapters.out.outbox.messaging.OutboxMarker;
import de.demo.lending.common.application.dto.DtoPayload;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final OutboxMarker marker;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafka, OutboxMarker marker) {
        this.kafka = kafka;
        this.marker = marker;
    }

    /*public void enqueue(long eventId, String type, Object payload) {
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
    }*/

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void enqueue(String topic, DtoPayload payload) {
        String kafkaKey = payload.getEventId().toString();

        CompletableFuture<SendResult<String, String>> future = kafka.send(topic, kafkaKey, payload.toString());

        // Callback: bei Erfolg -> flag setzen; bei Fehler -> attempt_count++
        future.thenAccept(result -> {

                    //publisher.enqueue(LOAN_REQUESTED_V1, payload);
                    marker.markAsSent(payload.getEventId(), Instant.now());
                })
                .exceptionally(ex -> {
                    marker.incrementAttempt(payload.getEventId(), ex.getMessage());
                    return null;
                });
    }
}
