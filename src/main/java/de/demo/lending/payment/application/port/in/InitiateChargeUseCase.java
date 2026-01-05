package de.demo.lending.payment.application.port.in;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.payment.domain.Payment;

public interface InitiateChargeUseCase {

    Payment initiate(InitiateChargeCommand command);

    /**
     * Command für Procurement-Initiierung
     * <p>
     * Immutable Value Object das alle notwendigen Daten
     * für die Initiierung eines Procurement-Prozesses enthält.
     */
    final class InitiateChargeCommand {

        private final LoanId loanId;
        private final UserId userId;

        private InitiateChargeCommand(
                LoanId loanId,
                UserId userId
        ) {
            this.loanId = requireNonNull(loanId, "loanId must not be null");
            this.userId = requireNonNull(userId, "userId must not be null");
        }

        // Factory Method
        public static InitiateChargeUseCase.InitiateChargeCommand of(
                LoanId loanId,
                UserId userId
        ) {
            return new InitiateChargeUseCase.InitiateChargeCommand(loanId, userId);
        }

        // Getters

        public LoanId getLoanId() {
            return loanId;
        }

        public UserId getUserId() {
            return userId;
        }

        // Equality

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InitiateChargeUseCase.InitiateChargeCommand that = (InitiateChargeUseCase.InitiateChargeCommand) o;
            return Objects.equals(loanId, that.loanId) &&
                    Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(loanId, userId);
        }

        @Override
        public String toString() {
            return "InitiateChargeCommand{" +
                    "loanId=" + loanId +
                    "userId=" + userId +
                    '}';
        }
    }

    /**
     * Result des Procurement-Initiierungs-Prozesses
     */
    final class PaymentResult {

        private final boolean success;
        private final LoanId loanId;
        private final String failureReason;

        private PaymentResult(
                boolean success,
                LoanId loanId,
                String failureReason
        ) {
            this.success = success;
            this.loanId = loanId;
            this.failureReason = failureReason;
        }

        public static InitiateChargeUseCase.PaymentResult success(LoanId loanId) {
            return new InitiateChargeUseCase.PaymentResult(true, loanId, null);
        }

        public static InitiateChargeUseCase.PaymentResult notAvailable() {
            return new InitiateChargeUseCase.PaymentResult(
                    false,
                    null,
                    "Book not available in external libraries"
            );
        }

        public static InitiateChargeUseCase.PaymentResult failed(String reason) {
            return new InitiateChargeUseCase.PaymentResult(false, null, reason);
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
