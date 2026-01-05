package de.demo.lending.loan.application.ports.in;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.LoanId;

public interface ActivateLoanUseCase {
    void activate(ActivateLoanCommand command);

    /**
     * Command für Procurement-Initiierung
     * <p>
     * Immutable Value Object das alle notwendigen Daten
     * für die Initiierung eines Procurement-Prozesses enthält.
     */
    final class ActivateLoanCommand {

        private final LoanId loanId;
        private final CopyId copyId;

        private ActivateLoanCommand(
                LoanId loanId,
                CopyId copyId
        ) {
            this.loanId = requireNonNull(loanId, "loanId must not be null");
            this.copyId = requireNonNull(copyId, "copyId must not be null");
        }

        // Factory Method
        public static ActivateLoanUseCase.ActivateLoanCommand of(
                LoanId loanId,
                CopyId copyId
        ) {
            return new ActivateLoanUseCase.ActivateLoanCommand(loanId, copyId);
        }

        // Getters

        public LoanId getLoanId() {
            return loanId;
        }

        public CopyId getCopyId() {
            return copyId;
        }

        // Equality

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ActivateLoanUseCase.ActivateLoanCommand that = (ActivateLoanUseCase.ActivateLoanCommand) o;
            return Objects.equals(loanId, that.loanId) &&
                    Objects.equals(copyId, that.copyId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(loanId, copyId);
        }

        @Override
        public String toString() {
            return "InitiateProcurementCommand{" +
                    "loanId=" + loanId +
                    "copyId=" + copyId +
                    '}';
        }
    }

    /**
     * Result des Procurement-Initiierungs-Prozesses
     */
    final class LoanResult {

        private final boolean success;
        private final LoanId loanId;
        private final String failureReason;

        private LoanResult(
                boolean success,
                LoanId loanId,
                String failureReason
        ) {
            this.success = success;
            this.loanId = loanId;
            this.failureReason = failureReason;
        }

        public static ActivateLoanUseCase.LoanResult success(LoanId loanId) {
            return new ActivateLoanUseCase.LoanResult(true, loanId, null);
        }

        public static ActivateLoanUseCase.LoanResult notAvailable() {
            return new ActivateLoanUseCase.LoanResult(
                    false,
                    null,
                    "Book not available in external libraries"
            );
        }

        public static ActivateLoanUseCase.LoanResult failed(String reason) {
            return new ActivateLoanUseCase.LoanResult(false, null, reason);
        }

        public boolean isSuccess() {
            return success;
        }

        public LoanId getLLoanId() {
            if (!success) {
                throw new IllegalStateException("No procurement order ID available for failed result");
            }
            return loanId;
        }

        public String getFailureReason() {
            if (success) {
                throw new IllegalStateException("No failure reason available for successful result");
            }
            return failureReason;
        }
    }
}
