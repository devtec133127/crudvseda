package de.demo.lending.inventory.domain.port.in.register_book;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.inventory.domain.Isbn;
import de.demo.lending.loan.domain.LoanId;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Command für Procurement-Initiierung
 * <p>
 * Immutable Value Object das alle notwendigen Daten
 * für die Initiierung eines Procurement-Prozesses enthält.
 */
public final class RegisterBookCommand {

    private final LoanId loanId;
    private final BookId bookId;
    private final Isbn isbn;

    private RegisterBookCommand(
            LoanId loanId,
            BookId bookId,
            Isbn isbn
    ) {
        this.loanId = requireNonNull(loanId, "loanId must not be null");
        this.bookId = requireNonNull(bookId, "bookId must not be null");
        this.isbn = requireNonNull(isbn, "isbn must not be null");
    }

    // Factory Method
    public static RegisterBookCommand of(
            LoanId loanId,
            BookId bookId,
            Isbn isbn
    ) {
        return new RegisterBookCommand(loanId, bookId, isbn);
    }

    // Getters

    public LoanId getLoanId() {
        return loanId;
    }

    public BookId getBookId() {
        return bookId;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    // Equality

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterBookCommand that = (RegisterBookCommand) o;
        return Objects.equals(loanId, that.loanId) &&
                Objects.equals(isbn, that.isbn) &&
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
                ", isbn=" + isbn +
                '}';
    }
}