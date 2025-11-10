package de.demo.lending.read.adapters.out.persistence;

import de.demo.lending.read.application.port.LoanStatusReadPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LoanStatusReadReporitoryAdapter implements LoanStatusReadPort {

    private final LoanStatusReadRepository repo;

    public LoanStatusReadReporitoryAdapter(LoanStatusReadRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<LoanStatusRead> findByCustomerIdOrderByUpdatedAtDesc(UUID customerId) {
        return repo.findByCustomerIdOrderByUpdatedAtDesc(customerId);
    }

    @Override
    public Optional<LoanStatusRead> findByLoanId(UUID loanId) {
        return repo.findByLoanId(loanId);
    }
}
