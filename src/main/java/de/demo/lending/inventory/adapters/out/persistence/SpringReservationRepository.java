package de.demo.lending.inventory.adapters.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringReservationRepository extends JpaRepository<ReservationEntity, UUID> {
}