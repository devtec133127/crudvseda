package de.demo.lending.loan.domain.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.loan.domain.LoanId;

public class LoanActivated extends BaseDomainEvent {
    private final CopyId copyId;
    private final LocalDate dueDate;

    public LoanActivated(LoanId loanId, CopyId copyId, LocalDate dueDate) {
        super(UUID.randomUUID(), loanId, "", "", Instant.now(), null);
        this.copyId = copyId;
        this.dueDate = dueDate;
    }

    public CopyId getCopyId() {
        return copyId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}
