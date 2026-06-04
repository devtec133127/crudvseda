package de.demo.lending.catalog.application;

public record BookSearchResult(
        String isbn,
        String title,
        String author,
        String coverUrl,
        String openLibraryKey
) {}
