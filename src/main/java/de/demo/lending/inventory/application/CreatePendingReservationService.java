package de.demo.lending.inventory.application;

import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.ports.in.CreatePendingReservationUseCase;
import de.demo.lending.inventory.application.ports.out.PendingReservationRepository;
import de.demo.lending.inventory.domain.PendingReservation;
import de.demo.lending.inventory.domain.PendingReservationId;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CreatePendingReservationService implements CreatePendingReservationUseCase {
    private final PendingReservationRepository pendingRepo;

    public CreatePendingReservationService(PendingReservationRepository pendingRepo) {
        this.pendingRepo = pendingRepo;
    }

    @Transactional
    public PendingReservationId create(
            Isbn isbn,
            LoanId loanId,
            UserId userId,
            long dueDate
    ) {
        // Prüfen ob schon eine Pending Reservation für diese ISBN existiert
        /*if (pendingRepo.findByBookTitle(bookTitle).isPresent()) {
            throw new RuntimeException("Pending Reservation for BookTitel " + bookTitle.toString() + " already exists");
        }*/

        // ⭐ Aggregate Root erstellen
        var pending = PendingReservation.create(
                isbn,
                loanId,
                userId,
                dueDate
        );

        // ⭐ HIER WIRD GESPEICHERT!
        pendingRepo.save(pending);

        return pending.getPendingReservationId();
    }
}
