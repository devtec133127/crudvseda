package de.demo.lending.inventory.domain.port.in;

import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

public interface ReserveBookUseCase {
    void reserveBook(UserId userId, LoanId loanId, BookTitle bookTitle);

    void reserveBook(UserId userId, LoanId loanId, BookTitle bookTitle, long durationInDays);
}
