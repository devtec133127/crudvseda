package de.demo.lending.common.adapters.out.outbox.messaging;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxMarker {
    private final OutboxRepository repo;

    @Autowired
    public OutboxMarker(OutboxRepository repo) {
        this.repo = repo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsSent(long messageId, Instant sentAt) {

        repo.findById(messageId).ifPresent(e -> {
            e.setPublishedAt(sentAt);
            repo.save(e);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAttempt(long messageId, String errorMessage) {
        repo.findById(messageId).ifPresent(e -> {
            e.setAttempt(e.getAttempt() + 1);
            e.setErrorMessage(errorMessage);
            repo.save(e);
        });
    }
}
