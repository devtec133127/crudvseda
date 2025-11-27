package de.demo.lending.procurement.domain.port.in;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrderId;
import jakarta.transaction.Transactional;

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
        private final BookTitle bookTitle;

        private InitiateProcurementCommand(
                LoanId loanId,
                BookTitle bookTitle
        ) {
            this.loanId = requireNonNull(loanId, "loanId must not be null");
            this.bookTitle = requireNonNull(bookTitle, "bookTitle must not be null");
        }

        // Factory Method
        public static InitiateProcurementCommand of(
                LoanId loanId,
                BookTitle bookTitle
        ) {
            return new InitiateProcurementCommand(loanId, bookTitle);
        }

        // Getters

        public LoanId getLoanId() {
            return loanId;
        }

        public BookTitle getBookTitle() {
            return bookTitle;
        }

        // Equality

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InitiateProcurementCommand that = (InitiateProcurementCommand) o;
            return Objects.equals(loanId, that.loanId) &&
                    Objects.equals(bookTitle, that.bookTitle);
        }

        @Override
        public int hashCode() {
            return Objects.hash(loanId, bookTitle);
        }

        @Override
        public String toString() {
            return "InitiateProcurementCommand{" +
                    "loanId=" + loanId +
                    ", bookTitle=" + bookTitle +
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
