package de.demo.lending.controller;

import de.demo.lending.domain.Book;
import de.demo.lending.dto.LoanRequest;
import de.demo.lending.service.LendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/lending")
public class LendingController {

    @Autowired
    private LendingService lendingService;

    // CREATE Book
    @PostMapping("/books")
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        return ResponseEntity.ok(lendingService.createBook(book.getTitle(), book.getIsbn()));
    }

    // READ All Books
    @GetMapping("/books")
    public ResponseEntity<Iterable<Book>> getAllBooks() {
        return ResponseEntity.ok(lendingService.getAllBooks());
    }

    // Szenario 1: Ausleih-Anfrage
    @PostMapping("/loans/request")
    public ResponseEntity<Book> requestLoan(@RequestBody LoanRequest request) {
        return ResponseEntity.ok(lendingService.requestLoan(request));
    }

    // Szenario 2: Verlängerung
    @PutMapping("/loans/{bookId}/extend")
    public ResponseEntity<Book> extendLoan(@PathVariable UUID bookId, @RequestParam UUID userId) {
        return ResponseEntity.ok(lendingService.extendLoan(bookId, userId));
    }

    // Szenario 3: Bestand abfragen
    @GetMapping("/inventory/{userId}")
    public ResponseEntity<Iterable<Book>> getUserInventory(@PathVariable UUID userId) {
        return ResponseEntity.ok(lendingService.getUserInventory(userId));
    }

    @GetMapping("/inventory/available")
    public ResponseEntity<Iterable<Book>> getAvailableBooks() {
        return ResponseEntity.ok(lendingService.getAvailableBooks());
    }

    // DELETE/RETURN
    @DeleteMapping("/books/{bookId}/return")
    public ResponseEntity<Book> returnBook(@PathVariable UUID bookId) {
        return ResponseEntity.ok(lendingService.returnBook(bookId));
    }
}