package de.demo.lending.common.adapters.out.outbox.messaging;

import java.time.Instant;
import java.util.ArrayList;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="outbox.publisher.enabled", havingValue="true", matchIfMissing=true)
public class OutboxScheduler {
    private final OutboxRepository repo;
    private final KafkaTemplate<String,String> kafka;
    @Value("${outbox.batch-size:100}") int batch;

    public OutboxScheduler(OutboxRepository repo, KafkaTemplate<String,String> kafka){
        this.repo=repo; this.kafka=kafka;
    }

    @Scheduled(fixedDelayString="${outbox.publish-interval-ms:500}")
    @Transactional
    public void publishBatch(){
        var batchRows = repo.findUnpublished(batch);
        var sentIds = new ArrayList<Long>(batchRows.size());
        for (var e : batchRows) {
            kafka.send(e.getType(), e.getPayload());
            sentIds.add(e.getId());
        }
        if(!sentIds.isEmpty()) repo.markPublished(sentIds, Instant.now());
    }
}