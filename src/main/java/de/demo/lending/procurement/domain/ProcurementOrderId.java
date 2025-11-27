package de.demo.lending.procurement.domain;

import de.demo.lending.common.valueobjects.UuidId;

import java.util.UUID;

public class ProcurementOrderId extends UuidId {
    protected ProcurementOrderId(UUID value) {
        super(value);
    }

    public static ProcurementOrderId newId() {
        return new ProcurementOrderId(UUID.randomUUID());
    }

    public static ProcurementOrderId of(UUID value) {
        return new ProcurementOrderId(value);
    }
}
