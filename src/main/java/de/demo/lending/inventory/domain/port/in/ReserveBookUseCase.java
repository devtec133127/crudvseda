package de.demo.lending.inventory.domain.port.in;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.loan.domain.LoanId;

public interface ReserveBookUseCase {
    void reserveBook(LoanId loanId, BookId bookId, UserId userId, InventoryCopy copy);

    void reserveBook(UserId userId, LoanId loanId, BookId bookId, long durationInDays);
}
