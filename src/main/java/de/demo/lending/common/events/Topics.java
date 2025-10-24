package de.demo.lending.common.events;

public final class Topics {
    private Topics() {
    }

    public static final String LOAN_REQUESTED_V1 = "loan.requested.v1";
    public static final String RESERVATION_CREATED_V1 = "reservation.created.v1";
    public static final String INVENTORY_RESERVED_V1 = "inventory.reserved.v1";
    public static final String INVENTORY_REJECTED_V1 = "inventory.rejected.v1";
    public static final String PAYMENT_V1 = "payment.v1";
    public static final String LOAN_CHECKED_OUT_V1 = "loan.checked_out.v1";
    public static final String LOAN_RETURNED_V1 = "loan.returned.v1";
    // ... andere Topics
}