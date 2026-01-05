package de.demo.lending.inventory.application.ports.in;

import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.inventory.domain.ReservationId;

/**
 * Result des Procurement-Initiierungs-Prozesses
 */
public final class InventoryResult {

    private final boolean success;
    private final InventoryCopy copy;
    private final String failureReason;
    private final ReservationId reservationId;

    private InventoryResult(
            boolean success,
            InventoryCopy copy,
            ReservationId reservationId,
            String failureReason
    ) {
        this.success = success;
        this.copy = copy;
        this.reservationId = reservationId;
        this.failureReason = failureReason;
    }

    public static InventoryResult success(InventoryCopy copy) {
        return new InventoryResult(true, copy, null, null);
    }

    public static InventoryResult success(ReservationId reservationId) {
        return new InventoryResult(true, null, reservationId, null);
    }

    public static InventoryResult notAvailable() {
        return new InventoryResult(
                false,
                null,
                null,
                "Book not available in external libraries"
        );
    }

    public static InventoryResult failed(String reason) {
        return new InventoryResult(false, null, null, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public InventoryCopy getCopy() {
        if (!success) {
            throw new IllegalStateException("No procurement order ID available for failed result");
        }
        return copy;
    }

    public String getFailureReason() {
        if (success) {
            throw new IllegalStateException("No failure reason available for successful result");
        }
        return failureReason;
    }
}
