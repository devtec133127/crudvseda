package de.demo.lending.procurement.domain.port.in;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrderId;
import jakarta.transaction.Transactional;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public interface InitiateProcurementUseCase {

    @Transactional
    ProcurementResult execute(InitiateProcurementCommand command);

    /**
     * Command für Procurement-Initiierung
     * <p>
     * Immutable Value Object das alle notwendigen Daten
     * für die Initiierung eines Procurement-Prozesses enthält.
     */
    final class InitiateProcurementCommand {

        private final LoanId loanId;
        private final UserId userId;
        private final BookId bookId;

        private InitiateProcurementCommand(
                LoanId loanId,
                UserId userId,
                BookId bookId
        ) {
            this.loanId = requireNonNull(loanId, "loanId must not be null");
            this.userId = requireNonNull(userId, "userId must not be null");
            this.bookId = requireNonNull(bookId, "bookId must not be null");
        }

        // Factory Method
        public static InitiateProcurementCommand of(
                LoanId loanId,
                UserId userId,
                BookId bookId
        ) {
            return new InitiateProcurementCommand(loanId, userId, bookId);
        }

        // Getters

        public LoanId getLoanId() {
            return loanId;
        }

        public UserId getUserId() {
            return userId;
        }

        public BookId getBookId() {
            return bookId;
        }

        // Equality

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InitiateProcurementCommand that = (InitiateProcurementCommand) o;
            return Objects.equals(loanId, that.loanId) &&
                    Objects.equals(userId, that.userId) &&
                    Objects.equals(bookId, that.bookId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(loanId, userId, bookId);
        }

        @Override
        public String toString() {
            return "InitiateProcurementCommand{" +
                    "loanId=" + loanId +
                    "userId=" + userId +
                    ", bookId=" + bookId +
                    '}';
        }
    }

    /**
     * Result des Procurement-Initiierungs-Prozesses
     */
    final class ProcurementResult {

        private final boolean success;
        private final ProcurementOrderId procurementOrderId;
        private final String failureReason;

        private ProcurementResult(
                boolean success,
                ProcurementOrderId procurementOrderId,
                String failureReason
        ) {
            this.success = success;
            this.procurementOrderId = procurementOrderId;
            this.failureReason = failureReason;
        }

        public static ProcurementResult success(ProcurementOrderId procurementOrderId) {
            return new ProcurementResult(true, procurementOrderId, null);
        }

        public static ProcurementResult notAvailable() {
            return new ProcurementResult(
                    false,
                    null,
                    "Book not available in external libraries"
            );
        }

        public static ProcurementResult failed(String reason) {
            return new ProcurementResult(false, null, reason);
        }

        public boolean isSuccess() {
            return success;
        }

        public ProcurementOrderId getProcurementOrderId() {
            if (!success) {
                throw new IllegalStateException("No procurement order ID available for failed result");
            }
            return procurementOrderId;
        }

        public String getFailureReason() {
            if (success) {
                throw new IllegalStateException("No failure reason available for successful result");
            }
            return failureReason;
        }
    }
}
