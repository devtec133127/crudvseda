package de.demo.lending.procurement.domain.port.out;


import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrder;

public interface ProcurementOrderRepository {

    ProcurementOrder findByLoanId(LoanId loanId);

    /**
     * Persistiert Aktualisierungen einer InventoryCopy (z.B. Statuswechsel).
     */
    void save(ProcurementOrder order);
}