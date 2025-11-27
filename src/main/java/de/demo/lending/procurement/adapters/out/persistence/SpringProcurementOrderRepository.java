package de.demo.lending.procurement.adapters.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringProcurementOrderRepository extends JpaRepository<ProcurementOrderEntity, UUID> {
}