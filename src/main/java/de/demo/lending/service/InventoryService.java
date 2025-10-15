package de.demo.lending.service;

import de.demo.lending.domain.Book;
import de.demo.lending.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private BookRepository bookRepository;

    // Prüfe Verfügbarkeit eines Buches
    public boolean checkAvailability(String bookTitle) {
        Optional<Book> book = bookRepository.findByTitle(bookTitle);
        return book.map(b -> b.isAvailable()).orElse(false);
    }

    // Hole verfügbare Bücher für Bestandsabfrage
    public Iterable<Book> getAvailableInventory() {
        return bookRepository.findAllByAvailable(true);
    }

    // Aktualisiere Bestand nach Ausleihe (intern)
    public void updateStock(String bookTitle, boolean available) {
        Optional<Book> book = bookRepository.findByTitle(bookTitle);
        book.ifPresent(b -> {
            b.setAvailable(available);
            bookRepository.save(b);
        });
    }

    // Prüfe User-Bestand
    public Iterable<Book> getUserStock(UUID userId) {
        return bookRepository.findByUserId(userId);
    }
}