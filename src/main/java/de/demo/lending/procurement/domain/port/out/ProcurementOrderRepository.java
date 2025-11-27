package de.demo.lending.procurement.domain.port.out;


import de.demo.lending.procurement.domain.ProcurementOrder;

public interface ProcurementOrderRepository {

    /**
     * Persistiert Aktualisierungen einer InventoryCopy (z.B. Statuswechsel).
     */
    void save(ProcurementOrder order);
}