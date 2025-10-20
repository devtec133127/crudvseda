package de.demo.lending.inventory.domain.event;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;

public record BookReserved (
        LoanId loanId,
        UserId userId,
        BookId bookId,
        String bookTitle,
        Instant occurredAt
) {}
