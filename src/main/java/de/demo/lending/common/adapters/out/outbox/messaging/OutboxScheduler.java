package de.demo.lending.common.adapters.out.outbox.messaging;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Scheduler für Outbox-Pattern (Kafka-Modus).
 * Pollt regelmäßig die Outbox-Tabelle und sendet Events an Kafka.
 * <p>
 * Aktiviert durch Profile "kafka" (Standard für Produktion).
 * Bei Profile "async" nicht aktiv, da Events direkt im Memory verteilt werden.
 */
@Slf4j
@Component
@Profile("kafka")  // NEU: Nur aktiv bei Kafka-Profil
//@ConditionalOnProperty(name="outbox.publisher.enabled", havingValue="true", matchIfMissing=true)
public class OutboxScheduler {
    private final OutboxRepository repo;
    private final OutboxMarker outboxMarker;
    //private final EventPublisher publisher;
    private final KafkaTemplate<String, String> kafka;

    @Value("${outbox.batch-size:100}")
    int batch;

    public OutboxScheduler(OutboxRepository repo, OutboxMarker marker, KafkaTemplate<String, String> kafka) {
        this.repo = repo;
        this.outboxMarker = marker;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${outbox.publish-interval-ms:500}")
    @Transactional
    public void publishBatch() {
        List<OutboxEntity> batchRows = repo.findUnpublished(batch);

        for (OutboxEntity e : batchRows) {
            ProducerRecord<String, String> record = new ProducerRecord<>("orders-topic", e.getType(), e.getPayload());
            CompletableFuture<SendResult<String, String>> future = kafka.send(record);

            // Callback: bei Erfolg -> flag setzen; bei Fehler -> attempt_count++
            future.thenAccept(result -> {

                        //publisher.enqueue(LOAN_REQUESTED_V1, payload);
                        outboxMarker.markAsSent(e.getId(), Instant.now());
                    })
                    .exceptionally(ex -> {
                        outboxMarker.incrementAttempt(e.getId(), ex.getMessage());
                        return null;
                    });
        }
    }
}
