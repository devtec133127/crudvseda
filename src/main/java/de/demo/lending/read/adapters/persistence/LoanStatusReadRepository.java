package de.demo.lending.read.adapters.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanStatusReadRepository extends JpaRepository<LoanStatusRead, UUID> {
    List<LoanStatusRead> findByCustomerIdOrderByUpdatedAtDesc(UUID customerId);
}
