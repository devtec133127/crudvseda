package de.demo.lending.inventory.domain.port.in.reserve_book;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.loan.domain.LoanId;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Command für Procurement-Initiierung
 * <p>
 * Immutable Value Object das alle notwendigen Daten
 * für die Initiierung eines Procurement-Prozesses enthält.
 */
public final class ReserveBookCommand {

    private final LoanId loanId;
    private final BookId bookId;
    private final UserId userId;
    private final InventoryCopy copy;

    private ReserveBookCommand(
            LoanId loanId,
            BookId bookId,
            UserId userId,
            InventoryCopy copy
    ) {
        this.loanId = requireNonNull(loanId, "loanId must not be null");
        this.bookId = requireNonNull(bookId, "bookId must not be null");
        this.userId = requireNonNull(userId, "userId must not be null");
        this.copy = requireNonNull(copy, "copy must not be null");
    }

    // Factory Method
    public static ReserveBookCommand of(
            LoanId loanId,
            BookId bookId,
            UserId userId,
            InventoryCopy copy
    ) {
        return new ReserveBookCommand(loanId, bookId, userId, copy);
    }

    // Getters

    public LoanId getLoanId() {
        return loanId;
    }

    public BookId getBookId() {
        return bookId;
    }

    public UserId getUserId() {
        return userId;
    }

    public InventoryCopy getCopy() {
        return copy;
    }

    // Equality

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReserveBookCommand that = (ReserveBookCommand) o;
        return Objects.equals(loanId, that.loanId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(copy, that.copy) &&
                Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId, bookId);
    }

    @Override
    public String toString() {
        return "InitiateProcurementCommand{" +
                "loanId=" + loanId +
                "bookId=" + bookId +
                "userId=" + userId +
                "copy=" + copy +
                '}';
    }
}