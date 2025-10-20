package de.demo.lending.inventory.domain.event;

import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;

public record ReservationCreated (
        LoanId loanId,
        UserId userId,
        String bookTitle,
        Instant occurredAt
) {}
