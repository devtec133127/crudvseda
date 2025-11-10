package de.demo.lending.read.application;

import de.demo.lending.read.adapters.out.persistence.LoanStatusRead;
import de.demo.lending.read.application.port.LoanStatusReadPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanStatusQueryService {

    private final LoanStatusReadPort readStatusPort;

    public LoanStatusQueryService(LoanStatusReadPort repository) {
        this.readStatusPort = repository;
    }

    public Optional<LoanStatusRead> getLoanStatus(UUID loanId) {
        return readStatusPort.findByLoanId(loanId);
    }

    public List<LoanStatusRead> getLoansForCustomer(UUID customerId) {
        return readStatusPort.findByCustomerIdOrderByUpdatedAtDesc(customerId);
    }
}
