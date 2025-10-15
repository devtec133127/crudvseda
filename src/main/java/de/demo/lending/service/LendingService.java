package de.demo.lending.service;

import de.demo.lending.domain.Book;
import de.demo.lending.dto.LoanRequest;
import de.demo.lending.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;


@Service
public class LendingService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryService inventoryService; // Neu

    @Autowired
    private PaymentService paymentService; // Neu

    // Szenario 1: Ausleih-Anfrage mit Kette
    public Book requestLoan(LoanRequest request) {
        // Schritt 1: Inventory prüfen (synchrone Kette)
        if (!inventoryService.checkAvailability(request.getBookTitle())) {
            throw new RuntimeException("Buch nicht verfügbar");
        }

        // Schritt 2: Payment verarbeiten (z. B. Kaution 5€)
        UUID userId = request.getUserId();
        if (!paymentService.processLoanPayment(userId, 5.0)) {
            throw new RuntimeException("Zahlung fehlgeschlagen");
        }

        // Schritt 3: Loan erstellen und Inventory updaten
        Optional<Book> optBook = bookRepository.findByTitle(request.getBookTitle());
        if (optBook.isPresent()) {
            Book book = optBook.get();
            book.setAvailable(false);
            book.setUserId(userId);
            book.setBorrowDate(LocalDate.now());
            book.setDueDate(LocalDate.now().plusWeeks(2));
            book = bookRepository.save(book);
            inventoryService.updateStock(request.getBookTitle(), false); // Bestand aktualisieren
            return book;
        }
        throw new RuntimeException("Buch nicht gefunden");
    }

    // Szenario 2: Verlängerung mit Kette
    public Book extendLoan(UUID bookId, UUID userId) {
        Optional<Book> optBook = bookRepository.findById(bookId);
        if (optBook.isPresent() && !optBook.get().isAvailable() && optBook.get().getUserId().equals(userId)) {
            Book book = optBook.get();
            if (!paymentService.processExtensionPayment(userId, 2.0)) { // Gebühr 2€
                throw new RuntimeException("Verlängerungsgebühr fehlgeschlagen");
            }
            book.setExtended(true);
            book.setDueDate(book.getDueDate().plusWeeks(1));
            book = bookRepository.save(book);
            inventoryService.updateStock(book.getTitle(), false); // Bestand bleibt unverändert
            return book;
        }
        throw new RuntimeException("Ausleihe nicht verlängerbar");
    }

    // Szenario 3: Bestand abfragen (über Inventory)
    public Iterable<Book> getUserInventory(UUID userId) {
        return inventoryService.getUserStock(userId);
    }

    public Iterable<Book> getAvailableBooks() {
        return inventoryService.getAvailableInventory();
    }

    // Rückgabe mit Rückerstattung
    public Book returnBook(UUID bookId) {
        Optional<Book> optBook = bookRepository.findById(bookId);
        if (optBook.isPresent() && !optBook.get().isAvailable()) {
            Book book = optBook.get();
            UUID userId = book.getUserId();
            paymentService.refundPayment(userId); // Rückerstattung
            book.setAvailable(true);
            book.setUserId(null);
            book.setBorrowDate(null);
            book.setDueDate(null);
            book.setExtended(false);
            book = bookRepository.save(book);
            inventoryService.updateStock(book.getTitle(), true); // Bestand aktualisieren
            return book;
        }
        throw new RuntimeException("Buch nicht ausgeliehen");
    }

    // CREATE und READ bleiben unverändert
    public Book createBook(String title, String isbn) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setAvailable(true);
        return bookRepository.save(book);
    }

    public Iterable<Book> getAllBooks() {
        return bookRepository.findAll();
    }
}