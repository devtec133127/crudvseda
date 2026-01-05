package de.demo.lending.inventory.application.ports.out;


import java.util.Optional;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.inventory.domain.InventoryCopy;

public interface InventoryRepository {
    /**
     * Versucht eine verfügbare Kopie für das Buch zu reservieren.
     * Gibt die CopyId zurück, wenn erfolgreich, sonst Optional.empty().
     */
    Optional<CopyId> reserveFirstAvailable(BookId bookId);

    /**
     * Liefert eine Kopie als Domain-Objekt (z.B. für Read-Model oder Details).
     */
    Optional<InventoryCopy> findById(CopyId id);

    Optional<InventoryCopy> lookupForBookInLocal(String bookId);

    /**
     * Persistiert Aktualisierungen einer InventoryCopy (z.B. Statuswechsel).
     */
    void save(InventoryCopy copy);
}