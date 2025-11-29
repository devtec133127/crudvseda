package de.demo.lending.inventory.domain.port.out;

import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.inventory.domain.PendingReservation;
import de.demo.lending.inventory.domain.PendingReservationId;

import java.util.Optional;

public interface PendingReservationRepository {

    Optional<PendingReservation> findByBookTitle(BookTitle bookTitle);

    Optional<PendingReservation> findById(PendingReservationId id);

    PendingReservation save(PendingReservation pendingReservation);

    void deleteById(PendingReservationId id);
}

