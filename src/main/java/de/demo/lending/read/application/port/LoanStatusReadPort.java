package de.demo.lending.read.application.port;

import de.demo.lending.read.adapters.out.persistence.LoanStatusRead;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanStatusReadPort {
    List<LoanStatusRead> findByCustomerIdOrderByUpdatedAtDesc(UUID customerId);

    Optional<LoanStatusRead> findByLoanId(UUID orderId);
}
