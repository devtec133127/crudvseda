package de.demo.lending.inventory.domain.port.in.register_book;

import de.demo.lending.inventory.domain.port.in.InventoryResult;

public interface RegisterBookUseCase {
    InventoryResult registerBook(RegisterBookCommand command);
}
