package de.demo.lending.payment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringPaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    /**
     * Liefert alle verfügbaren Copies für ein Buch, geordnet nach createdAt (älteste zuerst).
     * Wir geben eine Liste zurück, damit die Adapter-Logik entscheiden kann welche Kopie reserviert wird.
     */
    //@Query("select i from InventoryCopyEntity i where i.bookId = :bookId and i.state = 'AVAILABLE' order by i.createdAt")
    //List<ReservationEntity> findAvailableByBookId(@Param("bookId") String bookId);
}