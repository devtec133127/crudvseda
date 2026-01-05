package de.demo.lending.inventory.domain.alternative;

import java.time.LocalDate;

import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.Isbn;

/**
 * Entity (kein Aggregate Root!)
 * - Hat eigene Identity (CopyId)
 * - Kann nur über Book Aggregate manipuliert werden
 * - Lifecycle ist an Book gebunden
 */
public class BookCopy {

    private final CopyId copyId;        // <- Identity!
    private final Isbn isbn;
    private CopyStatus status;
    private final LocalDate acquisitionDate;
    private String location;

    // Package-private constructor! Nur Book kann Copies erstellen
    BookCopy(CopyId copyId, Isbn isbn, CopyStatus status,
             LocalDate acquisitionDate, String location) {
        this.copyId = copyId;
        this.isbn = isbn;
        this.status = status;
        this.acquisitionDate = acquisitionDate;
        this.location = location;
    }

    /**
     * Business Method: Copy reservieren
     * Package-private! Nur über Book Aggregate aufrufbar
     */
    void reserve() {
        if (this.status != CopyStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Copy kann nicht reserviert werden. Status: " + this.status
            );
        }
        this.status = CopyStatus.RESERVED;
    }

    void makeAvailable() {
        if (this.status != CopyStatus.RESERVED && this.status != CopyStatus.ON_LOAN) {
            throw new IllegalStateException(
                    "Ungültiger Status-Übergang: " + this.status + " -> AVAILABLE"
            );
        }
        this.status = CopyStatus.AVAILABLE;
    }

    void markAsOnLoan() {
        if (this.status != CopyStatus.RESERVED) {
            throw new IllegalStateException(
                    "Nur reservierte Copies können ausgeliehen werden"
            );
        }
        this.status = CopyStatus.ON_LOAN;
    }

    // Public getters
    public CopyId getCopyId() {
        return copyId;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookCopy bookCopy = (BookCopy) o;
        return copyId.equals(bookCopy.copyId);  // Identity-based!
    }

    @Override
    public int hashCode() {
        return copyId.hashCode();
    }
}