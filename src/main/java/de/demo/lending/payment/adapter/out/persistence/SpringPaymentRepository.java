package de.demo.lending.payment.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringPaymentRepository extends JpaRepository<PaymentEntity, UUID> {
}