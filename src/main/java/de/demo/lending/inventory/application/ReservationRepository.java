package de.demo.lending.inventory.application;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.inventory.domain.Reservation;

import java.util.Optional;

public interface ReservationRepository {

    /**
     * Liefert eine Kopie als Domain-Objekt (z.B. für Read-Model oder Details).
     */
    Optional<InventoryCopy> findById(CopyId id);

    /**
     * Persistiert Aktualisierungen einer InventoryCopy (z.B. Statuswechsel).
     */
    void save(Reservation reservation);
}