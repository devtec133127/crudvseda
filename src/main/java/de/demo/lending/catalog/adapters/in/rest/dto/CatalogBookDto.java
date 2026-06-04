package de.demo.lending.catalog.adapters.in.rest.dto;

import java.time.Instant;

public record CatalogBookDto(
        String id,
        String isbn,
        String title,
        String author,
        String coverUrl,
        String openLibraryKey,
        Instant addedAt
) {}
