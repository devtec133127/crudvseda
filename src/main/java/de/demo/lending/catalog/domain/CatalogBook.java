package de.demo.lending.catalog.domain;

import java.time.Instant;

public class CatalogBook {

    private final CatalogBookId id;
    private final String isbn;
    private final String title;
    private final String author;
    private final String coverUrl;
    private final String openLibraryKey;
    private final Instant addedAt;

    private CatalogBook(
            CatalogBookId id,
            String isbn,
            String title,
            String author,
            String coverUrl,
            String openLibraryKey,
            Instant addedAt
    ) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Titel darf nicht leer sein");
        }
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.openLibraryKey = openLibraryKey;
        this.addedAt = addedAt;
    }

    public static CatalogBook add(
            String isbn,
            String title,
            String author,
            String coverUrl,
            String openLibraryKey
    ) {
        return new CatalogBook(
                CatalogBookId.newId(),
                isbn,
                title,
                author,
                coverUrl,
                openLibraryKey,
                Instant.now()
        );
    }

    public static CatalogBook restore(
            CatalogBookId id,
            String isbn,
            String title,
            String author,
            String coverUrl,
            String openLibraryKey,
            Instant addedAt
    ) {
        return new CatalogBook(id, isbn, title, author, coverUrl, openLibraryKey, addedAt);
    }

    public CatalogBookId getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverUrl() { return coverUrl; }
    public String getOpenLibraryKey() { return openLibraryKey; }
    public Instant getAddedAt() { return addedAt; }
}
