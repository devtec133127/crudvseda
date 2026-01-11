package de.demo.lending.loan.application.ports.out;

import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.loan.domain.Loan;

public interface LoanRepository {
    void save(Loan loan);

    Loan findByLoanId(LoanId loanId);
}
