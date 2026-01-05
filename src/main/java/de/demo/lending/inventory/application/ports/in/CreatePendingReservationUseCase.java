package de.demo.lending.inventory.application.ports.in;

import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.PendingReservationId;

public interface CreatePendingReservationUseCase {
    public PendingReservationId create(
            Isbn isbn,
            LoanId loanId,
            UserId userId,
            long dueDate
    );
}
