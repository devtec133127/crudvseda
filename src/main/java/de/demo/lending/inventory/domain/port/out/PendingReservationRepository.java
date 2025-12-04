package de.demo.lending.inventory.domain.port.out;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.PendingReservation;
import de.demo.lending.inventory.domain.PendingReservationId;
import de.demo.lending.loan.domain.LoanId;

import java.util.Optional;

public interface PendingReservationRepository {

    Optional<PendingReservation> findBookForLoan(BookId bookId, UserId userId, LoanId loanId);

    Optional<PendingReservation> findById(PendingReservationId id);

    PendingReservation save(PendingReservation pendingReservation);

    void deleteById(PendingReservationId id);
}

