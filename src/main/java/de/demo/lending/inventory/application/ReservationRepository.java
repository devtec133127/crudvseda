package de.demo.lending.inventory.application;

import de.demo.lending.inventory.domain.Reservation;


public interface ReservationRepository {

    /**
     * Liefert eine Kopie als Domain-Objekt (z.B. für Read-Model oder Details).
     */
    //Optional<InventoryCopy> findById(CopyId id);

    /**
     * Persistiert Aktualisierungen einer InventoryCopy (z.B. Statuswechsel).
     */
    Reservation save(Reservation reservation);
}