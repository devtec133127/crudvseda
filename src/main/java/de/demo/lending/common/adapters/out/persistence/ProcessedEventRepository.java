package de.demo.lending.common.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, String> {
    boolean existsByEventIdAndConsumer(String eventId, String consumer);
}
