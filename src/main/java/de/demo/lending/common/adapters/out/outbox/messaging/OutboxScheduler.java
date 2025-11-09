package de.demo.lending.common.adapters.out.outbox.messaging;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import jakarta.transaction.Transactional;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler für Outbox-Pattern (Kafka-Modus).
 * Pollt regelmäßig die Outbox-Tabelle und sendet Events an Kafka.
 * <p>
 * Aktiviert durch Profile "kafka" (Standard für Produktion).
 * Bei Profile "async" nicht aktiv, da Events direkt im Memory verteilt werden.
 */
@Component
@Profile("kafka")  // NEU: Nur aktiv bei Kafka-Profil
//@ConditionalOnProperty(name="outbox.publisher.enabled", havingValue="true", matchIfMissing=true)
public class OutboxScheduler {
    private final OutboxRepository repo;
    private final KafkaTemplate<String, String> kafka;
    private final OutboxMarker outboxMarker;
    @Value("${outbox.batch-size:100}")
    int batch;

    public OutboxScheduler(OutboxRepository repo, KafkaTemplate<String, String> kafka, OutboxMarker marker) {
        this.repo = repo;
        this.kafka = kafka;
        this.outboxMarker = marker;
    }

    @Scheduled(fixedDelayString = "${outbox.publish-interval-ms:500}")
    @Transactional
    public void publishBatch() {
        List<OutboxEntity> batchRows = repo.findUnpublished(batch);

        for (OutboxEntity e : batchRows) {

            ProducerRecord<String, String> record = new ProducerRecord<>("orders-topic", e.getType(), e.getPayload());
            // optional: headers z.B. message-id
            record.headers().add("message-id", e.getId().toString().getBytes(StandardCharsets.UTF_8));
            CompletableFuture<SendResult<String, String>> future = kafka.send(record);

            // Callback: bei Erfolg -> flag setzen; bei Fehler -> attempt_count++
            future.thenAccept(result -> outboxMarker.markAsSent(e.getId(), Instant.now()))
                    .exceptionally(ex -> {
                        outboxMarker.incrementAttempt(e.getId(), ex.getMessage());
                        return null;
                    });
        }
    }
}
