package de.demo.lending.loan.application;

import de.demo.lending.loan.domain.Loan;

public interface LoanRepository {
    void save(Loan loan);
}
