package de.demo.lending.inventory.domain.port.out;

import java.util.Optional;

import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.PendingReservation;
import de.demo.lending.inventory.domain.PendingReservationId;
import de.demo.lending.loan.domain.LoanId;

public interface PendingReservationRepository {

    Optional<PendingReservation> findBookForLoan(Isbn isbn, UserId userId, LoanId loanId);

    Optional<PendingReservation> findById(PendingReservationId id);

    PendingReservation save(PendingReservation pendingReservation);

    void deleteById(PendingReservationId id);
}

