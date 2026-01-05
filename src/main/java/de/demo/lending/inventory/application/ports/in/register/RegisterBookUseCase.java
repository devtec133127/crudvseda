package de.demo.lending.inventory.application.ports.in.register;

import de.demo.lending.inventory.application.ports.in.InventoryResult;

public interface RegisterBookUseCase {
    InventoryResult registerBook(RegisterBookCommand command);
}
