package de.demo.lending.loan.domain.event;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.*;
import java.time.Instant;

public record LoanRequested(
        LoanId loanId,
        UserId userId,
        String bookTitle,
        Instant occurredAt
) {}
