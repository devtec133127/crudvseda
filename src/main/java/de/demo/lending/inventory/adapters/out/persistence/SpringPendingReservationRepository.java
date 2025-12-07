package de.demo.lending.inventory.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SpringPendingReservationRepository extends JpaRepository<PendingReservationEntity, UUID> {
  
    @Query("select p from PendingReservationEntity p where p.isbn = :isbn and p.userId = :userId and p.loanId = :loanId")
    PendingReservationEntity findBookForLoan(@Param("isbn") String isbn,
                                             @Param("userId") UUID userId,
                                             @Param("loanId") UUID loanId);

}

