package de.demo.lending.inventory.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringPendingReservationRepository extends JpaRepository<PendingReservationEntity, UUID> {
    Optional<PendingReservationEntity> findByBookId(String bookId);

    @Query("select p from PendingReservationEntity p where p.bookId = :bookId and p.userId = :userId and p.loanId = :loanId")
    PendingReservationEntity findBookForLoan(@Param("bookId") String bookId,
                                             @Param("userId") UUID userId,
                                             @Param("loanId") UUID loanId);

}

