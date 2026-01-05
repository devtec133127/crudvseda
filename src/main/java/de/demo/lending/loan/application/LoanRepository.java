package de.demo.lending.loan.application;

import de.demo.lending.loan.domain.Loan;
import de.demo.lending.common.valueobjects.LoanId;

public interface LoanRepository {
    void save(Loan loan);

    Loan findByLoanId(LoanId loanId);
}
