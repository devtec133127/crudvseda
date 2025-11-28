package de.demo.lending.procurement.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SpringProcurementOrderRepository extends JpaRepository<ProcurementOrderEntity, UUID> {
    @Query("select p from ProcurementOrderEntity p where p.loanId = :loanId")
    ProcurementOrderEntity findByLoanId(@Param("loanId") UUID loanId);
}