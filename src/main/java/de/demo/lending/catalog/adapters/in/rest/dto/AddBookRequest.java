package de.demo.lending.catalog.adapters.in.rest.dto;

public record AddBookRequest(
        String isbn,
        String title,
        String author,
        String coverUrl,
        String openLibraryKey
) {}
