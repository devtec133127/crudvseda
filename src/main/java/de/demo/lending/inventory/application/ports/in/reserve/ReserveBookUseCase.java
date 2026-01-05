package de.demo.lending.inventory.application.ports.in.reserve;

import de.demo.lending.inventory.application.ports.in.InventoryResult;

public interface ReserveBookUseCase {
    InventoryResult reserveBook(ReserveBookCommand command);

    InventoryResult reserveLocalBook(ReserveLocalBookCommand command);
}
