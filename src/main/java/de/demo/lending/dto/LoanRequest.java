package de.demo.lending.dto;

import java.util.UUID;

public class LoanRequest {
    private String bookTitle;
    private UUID userId;
    // Getter und Setter
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}

