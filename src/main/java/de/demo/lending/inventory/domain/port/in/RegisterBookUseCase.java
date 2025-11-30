package de.demo.lending.inventory.domain.port.in;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.loan.domain.LoanId;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public interface RegisterBookUseCase {
    InventoryCopy registerBook(RegisterBookCommand command);


    /**
     * Command für Procurement-Initiierung
     * <p>
     * Immutable Value Object das alle notwendigen Daten
     * für die Initiierung eines Procurement-Prozesses enthält.
     */
    final class RegisterBookCommand {

        private final LoanId loanId;
        private final BookId bookId;

        private RegisterBookCommand(
                LoanId loanId,
                BookId bookId
        ) {
            this.loanId = requireNonNull(loanId, "loanId must not be null");
            this.bookId = requireNonNull(bookId, "bookId must not be null");
        }

        // Factory Method
        public static RegisterBookCommand of(
                LoanId loanId,
                BookId bookId
        ) {
            return new RegisterBookCommand(loanId, bookId);
        }

        // Getters

        public LoanId getLoanId() {
            return loanId;
        }

        public BookId getBookId() {
            return bookId;
        }

        // Equality

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegisterBookCommand that = (RegisterBookCommand) o;
            return Objects.equals(loanId, that.loanId) &&
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
                    '}';
        }
    }

    /**
     * Result des Procurement-Initiierungs-Prozesses
     */
    final class InventoryResult {

        private final boolean success;
        private final CopyId copyId;
        private final String failureReason;

        private InventoryResult(
                boolean success,
                CopyId copyId,
                String failureReason
        ) {
            this.success = success;
            this.copyId = copyId;
            this.failureReason = failureReason;
        }

        public static RegisterBookUseCase.InventoryResult success(CopyId copyId) {
            return new RegisterBookUseCase.InventoryResult(true, copyId, null);
        }

        public static RegisterBookUseCase.InventoryResult notAvailable() {
            return new RegisterBookUseCase.InventoryResult(
                    false,
                    null,
                    "Book not available in external libraries"
            );
        }

        public static RegisterBookUseCase.InventoryResult failed(String reason) {
            return new RegisterBookUseCase.InventoryResult(false, null, reason);
        }

        public boolean isSuccess() {
            return success;
        }

        public CopyId getCopyId() {
            if (!success) {
                throw new IllegalStateException("No procurement order ID available for failed result");
            }
            return copyId;
        }

        public String getFailureReason() {
            if (success) {
                throw new IllegalStateException("No failure reason available for successful result");
            }
            return failureReason;
        }
    }
}
