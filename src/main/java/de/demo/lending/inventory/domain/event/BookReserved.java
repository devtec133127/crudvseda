package de.demo.lending.inventory.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.ReservationId;
import de.demo.lending.loan.domain.LoanId;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class BookReserved extends BaseDomainEvent {
    private final String loanId;
    private final String copyId;
    private final String bookTitle;
    private final String bookId;

    public BookReserved(LoanId loanId, String correlationId, String causationId,
                        CopyId copyId, String bookTitle, UserId userId, BookId bookId) {
        super(UUID.randomUUID().toString(), correlationId, causationId, Instant.now(), userId);
        this.loanId = loanId.value().toString();
        this.copyId = copyId.value().toString();
        this.bookTitle = Objects.requireNonNull(bookTitle);
        this.bookId = bookId.value().toString();
    }
}
