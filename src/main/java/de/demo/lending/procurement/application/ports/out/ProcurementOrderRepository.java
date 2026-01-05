package de.demo.lending.procurement.application.ports.out;


import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrder;

public interface ProcurementOrderRepository {

    ProcurementOrder findByLoanId(LoanId loanId);

    /**
     * Persistiert Aktualisierungen einer InventoryCopy (z.B. Statuswechsel).
     */
    void save(ProcurementOrder order);
}