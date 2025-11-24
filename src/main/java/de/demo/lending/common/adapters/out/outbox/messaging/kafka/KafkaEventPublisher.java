package de.demo.lending.common.adapters.out.outbox.messaging.kafka;

import java.time.Instant;
import java.util.List;

import de.demo.lending.common.adapters.out.outbox.messaging.OutboxEntity;
import de.demo.lending.common.adapters.out.outbox.messaging.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
public class KafkaEventPublisher {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    //private final KafkaTemplate<String, String> kafka;
    private final OutboxRepository outboxRepository;
    //private final OutboxMarker marker;

    public KafkaEventPublisher(OutboxRepository outboxRepository) {
        //this.kafka = kafka;
        this.outboxRepository = outboxRepository;
    }

    //@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Scheduled(fixedDelay = 1000)
    public void publish() {

        List<OutboxEntity> events = outboxRepository.findUnpublished(1);
        for (OutboxEntity event : events) {
            // fachlicher Key = z. B. loanId
            String key = event.getLoanId();

            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            try {
                transactionTemplate.execute(status -> {
                    kafkaTemplate.executeInTransaction(kt -> {
                        kafkaTemplate.send(event.getType(), key, event.getPayload());
                        return true;
                    });

                    event.setPublishedAt(Instant.now());
                    outboxRepository.save(event);

                    return null;
                });
            } catch (Exception e) {
                // Retry beim nächsten Scheduler-Lauf
                log.error("Failed to publish outbox event {}", event.getId(), e);
            }
        }

        /*String kafkaKey = payload.getEventId().toString();

        CompletableFuture<SendResult<String, String>> future = kafka.send(topic, kafkaKey, payload.toString());

        // Callback: bei Erfolg -> flag setzen; bei Fehler -> attempt_count++
        future.thenAccept(result -> {

                    //publisher.enqueue(LOAN_REQUESTED_V1, payload);
                    marker.markAsSent(payload.getEventId(), Instant.now());
                })
                .exceptionally(ex -> {
                    marker.incrementAttempt(payload.getEventId(), ex.getMessage());
                    return null;
                });*/
    }
}
