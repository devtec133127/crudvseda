package de.demo.lending.common.adapters.out.processed;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringProcessedEventRepository extends JpaRepository<ProcessedEventEntity, String> {
    boolean existsByEventIdAndConsumer(String eventId, String consumer);
}