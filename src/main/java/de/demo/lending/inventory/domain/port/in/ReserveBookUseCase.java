package de.demo.lending.inventory.domain.port.in;

import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Duration;

public interface ReserveBookUseCase {
    void reserveBook(UserId userId, LoanId loanId, String bookTitle,
                     Duration duration, String correlationId, String causationId);
}
