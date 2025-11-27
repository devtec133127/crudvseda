package de.demo.lending.inventory.domain.port.out;

import de.demo.lending.inventory.domain.Reservation;


public interface ReservationRepository {

    /**
     * Persistiert Aktualisierungen einer InventoryCopy (z.B. Statuswechsel).
     */
    Reservation save(Reservation reservation);
}