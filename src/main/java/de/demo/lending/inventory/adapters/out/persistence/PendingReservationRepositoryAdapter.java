package de.demo.lending.inventory.adapters.out.persistence;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.PendingReservation;
import de.demo.lending.inventory.domain.PendingReservationId;
import de.demo.lending.inventory.domain.port.out.PendingReservationRepository;
import de.demo.lending.loan.domain.LoanId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PendingReservationRepositoryAdapter implements PendingReservationRepository {

    private final SpringPendingReservationRepository jpa;

    public PendingReservationRepositoryAdapter(SpringPendingReservationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<PendingReservation> findBookForLoan(BookId bookId, UserId userId, LoanId loanId) {
        PendingReservationEntity bookForLoan = jpa.findBookForLoan(bookId.value(), userId.value(), loanId.value());
        if (bookForLoan == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(bookForLoan));
    }

    @Override
    public Optional<PendingReservation> findById(PendingReservationId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public PendingReservation save(PendingReservation pendingReservation) {
        PendingReservationEntity e = PendingReservationEntity.builder()
                .id(pendingReservation.getPendingReservationId().value())
                .loanId(pendingReservation.getLoanId().value())
                .bookId(pendingReservation.getBookId().value())
                .userId(pendingReservation.getUserId().value())
                .dueDate(pendingReservation.getDueDate())
                .build();

        PendingReservationEntity saved = jpa.save(e);
        return toDomain(saved);
    }

    @Override
    public void deleteById(PendingReservationId id) {
        jpa.deleteById(id.value());
    }

    private PendingReservation toDomain(PendingReservationEntity e) {
        // map entity -> domain
        PendingReservationId id = PendingReservationId.of(e.getId());
        LoanId loanId = (e.getLoanId() == null) ? null : LoanId.of(e.getLoanId());
        UserId userId = UserId.of(e.getUserId());
        BookId bookId = BookId.of(e.getBookId());
        return PendingReservation.reconstitute(id, bookId, loanId, userId, e.getDueDate());
    }
}

