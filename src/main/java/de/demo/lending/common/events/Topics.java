package de.demo.lending.common.events;

public final class Topics {
    private Topics() {
    }

    public static final String LOAN_REQUESTED_V1 = "loan.requested.v1";
    public static final String INVENTORY_BOOK_NOT_FOUND_V1 = "inventory.book_not_found.v1";
    public static final String INVENTORY_RESERVED_V1 = "inventory.reserved.v1";
    public static final String PROCUREMENT_INITIATED_V1 = "procurement.initiated.v1";
    public static final String BOOK_ORDERED_EXTERNALLY_V1 = "book.ordered_externally.v1";
    public static final String PAYMENT_V1 = "payment.v1";

    // ... andere Topics
}