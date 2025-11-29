package de.demo.lending.inventory.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringPendingReservationRepository extends JpaRepository<PendingReservationEntity, UUID> {
    Optional<PendingReservationEntity> findByBookTitle(String bookId);
}

