package de.demo.lending.inventory.application.ports.in.reserve;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.common.valueobjects.UserId;

/**
 * Command für Procurement-Initiierung
 * <p>
 * Immutable Value Object das alle notwendigen Daten
 * für die Initiierung eines Procurement-Prozesses enthält.
 */
public final class ReserveLocalBookCommand {

    private final LoanId loanId;
    private final Isbn isbn;
    private final UserId userId;
    private final long durationInDays;

    private ReserveLocalBookCommand(
            LoanId loanId,
            Isbn isbn,
            UserId userId,
            long durationInDays
    ) {
        this.loanId = requireNonNull(loanId, "loanId must not be null");
        this.isbn = requireNonNull(isbn, "isbn must not be null");
        this.userId = requireNonNull(userId, "userId must not be null");
        if (durationInDays <= 0) {
            throw new IllegalArgumentException("durationInDays must be greater than zero");
        }
        this.durationInDays = durationInDays;
    }

    // Factory Method
    public static ReserveLocalBookCommand of(
            LoanId loanId,
            Isbn isbn,
            UserId userId,
            long durationInDays
    ) {
        return new ReserveLocalBookCommand(loanId, isbn, userId, durationInDays);
    }

    // Getters

    public LoanId getLoanId() {
        return loanId;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    public UserId getUserId() {
        return userId;
    }

    public long getDurationInDays() {
        return durationInDays;
    }

    // Equality

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReserveLocalBookCommand that = (ReserveLocalBookCommand) o;
        return Objects.equals(loanId, that.loanId) &&
                Objects.equals(userId, that.userId) &&
                durationInDays == that.durationInDays &&
                Objects.equals(isbn, that.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId, isbn);
    }

    @Override
    public String toString() {
        return "InitiateProcurementCommand{" +
                "loanId=" + loanId +
                "isbn=" + isbn +
                "userId=" + userId +
                "durationInDays=" + durationInDays +
                '}';
    }
}