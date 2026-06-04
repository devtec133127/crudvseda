package de.demo.lending.catalog.application.ports.out;

import de.demo.lending.catalog.application.BookSearchResult;

import java.util.List;

public interface LibrarySearchClient {
    List<BookSearchResult> search(String query);
}
