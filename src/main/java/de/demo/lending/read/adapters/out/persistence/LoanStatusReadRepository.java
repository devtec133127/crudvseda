package de.demo.lending.read.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanStatusReadRepository extends JpaRepository<LoanStatusRead, UUID> {
    List<LoanStatusRead> findByCustomerIdOrderByUpdatedAtDesc(UUID customerId);

    Optional<LoanStatusRead> findByLoanId(UUID orderId);
}
