package de.demo.lending.inventory.domain.port.in;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.PendingReservationId;
import de.demo.lending.loan.domain.LoanId;

public interface CreatePendingReservationUseCase {
    public PendingReservationId create(
            BookId bookId,
            LoanId loanId,
            UserId userId,
            long dueDate
    );
}
