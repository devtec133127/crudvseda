package de.demo.lending.loan.application;

import de.demo.lending.loan.domain.Loan;
import de.demo.lending.loan.domain.LoanId;

import java.util.Optional;
import java.util.UUID;

public interface LoanRepository {
    void save(Loan loan);
    Optional<Loan> findById(LoanId id);
}
