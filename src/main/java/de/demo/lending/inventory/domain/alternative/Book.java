package de.demo.lending.inventory.domain.alternative;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.inventory.domain.InventoryCopy;

/**
 * ALTERNATIVE MODELLIERUNG (nicht verwendet im System):
 * <p>
 * Dieser Ansatz modelliert Book als Aggregate Root mit BookCopy Entities.
 * Vorteil: Alle Copies eines Buches werden zusammen verwaltet.
 * <p>
 * Im aktuellen System wurde stattdessen eine andere Lösung gewählt:
 * - InventoryCopy als eigenständiges Aggregate (bessere Skalierung)
 * - Reservation als eigenständiges Aggregate (klare Bounded Context Grenzen)
 * <p>
 * Dieser Code dient als Beispiel für:
 * - Entity vs Value Object Unterschiede
 * - Aggregate Root mit Child Entities
 * - Invarianten-Schutz innerhalb eines Aggregates
 *
 * @see InventoryCopy für die tatsächlich verwendete Lösung
 */
public class Book {

    private final BookId id;
    private final Isbn isbn;
    private final String title;
    private final String author;

    // Child Entities! Jedes Copy hat eigene Identity
    private final List<BookCopy> copies = new ArrayList<>();


    private Book(BookId id, Isbn isbn, String title, String author) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public static Book create(Isbn isbn, String title, String author) {
        return new Book(BookId.of("123"), isbn, title, author);
    }

    /**
     * Business Method: Neues Exemplar hinzufügen
     */
    public BookCopy addCopy(String location) {
        BookCopy newCopy = new BookCopy(
                CopyId.newId(),
                this.isbn,
                CopyStatus.AVAILABLE,
                LocalDate.now(),
                location
        );

        this.copies.add(newCopy);
        return newCopy;
    }

    /**
     * Business Method: Verfügbares Copy reservieren
     */
    public Optional<BookCopy> reserveAvailableCopy() {
        return copies.stream()
                .filter(copy -> copy.getStatus() == CopyStatus.AVAILABLE)
                .findFirst()
                .map(copy -> {
                    copy.reserve();  // Entity-Methode!
                    return copy;
                });
    }

    /**
     * Business Method: Copy zurückgeben
     */
    public void returnCopy(CopyId copyId) {
        BookCopy copy = findCopy(copyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Copy nicht gefunden: " + copyId
                ));

        copy.makeAvailable();  // Entity-Methode!
    }

    private Optional<BookCopy> findCopy(CopyId copyId) {
        return copies.stream()
                .filter(c -> c.getCopyId().equals(copyId))
                .findFirst();
    }

    // Defensive copy!
    public List<BookCopy> getCopies() {
        return Collections.unmodifiableList(copies);
    }

    public int getAvailableCopiesCount() {
        return (int) copies.stream()
                .filter(c -> c.getStatus() == CopyStatus.AVAILABLE)
                .count();
    }

    public BookId getId() {
        return id;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}