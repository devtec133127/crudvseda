package de.demo.lending.procurement.domain;

import java.util.UUID;

public record ProcurementOrderId(UUID value) {
    public ProcurementOrderId {
        if (value == null) {
            throw new IllegalArgumentException("ProcurementOrderId darf nicht null sein");
        }
    }

    public static ProcurementOrderId newId() {
        return new ProcurementOrderId(UUID.randomUUID());
    }

    public static ProcurementOrderId of(UUID value) {
        return new ProcurementOrderId(value);
    }
}
