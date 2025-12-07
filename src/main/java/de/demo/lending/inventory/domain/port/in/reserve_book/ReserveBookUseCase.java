package de.demo.lending.inventory.domain.port.in.reserve_book;

import de.demo.lending.inventory.domain.port.in.InventoryResult;

public interface ReserveBookUseCase {
    InventoryResult reserveBook(ReserveBookCommand command);

    InventoryResult reserveLocalBook(ReserveLocalBookCommand command);
}
