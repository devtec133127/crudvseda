package de.demo.lending.inventory.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringInventoryCopyRepository extends JpaRepository<InventoryCopyEntity, UUID> {
    /**
     * Liefert alle verfügbaren Copies für ein Buch, geordnet nach createdAt (älteste zuerst).
     * Wir geben eine Liste zurück, damit die Adapter-Logik entscheiden kann welche Kopie reserviert wird.
     */
    @Query("select i from InventoryCopyEntity i where i.bookId = :bookId and i.state = 'AVAILABLE' order by i.createdAt")
    List<InventoryCopyEntity> findAvailableByBookId(@Param("bookId") String bookId);

    @Query("select i from InventoryCopyEntity i where i.bookId = :bookId and i.state = 'AVAILABLE' order by i.createdAt")
    List<InventoryCopyEntity> findByBookId(@Param("bookI") String bookId);
}