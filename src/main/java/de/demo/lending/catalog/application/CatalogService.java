package de.demo.lending.catalog.application;

import de.demo.lending.catalog.application.ports.out.CatalogBookRepository;
import de.demo.lending.catalog.application.ports.out.LibrarySearchClient;
import de.demo.lending.catalog.domain.CatalogBook;
import de.demo.lending.catalog.domain.CatalogBookId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatalogService {

    private final CatalogBookRepository repository;
    private final LibrarySearchClient librarySearchClient;

    public CatalogService(CatalogBookRepository repository, LibrarySearchClient librarySearchClient) {
        this.repository = repository;
        this.librarySearchClient = librarySearchClient;
    }

    public List<BookSearchResult> search(String query) {
        return librarySearchClient.search(query);
    }

    @Transactional
    public CatalogBookId add(String isbn, String title, String author, String coverUrl, String openLibraryKey) {
        CatalogBook book = CatalogBook.add(isbn, title, author, coverUrl, openLibraryKey);
        repository.save(book);
        return book.getId();
    }

    public List<CatalogBook> findAll() {
        return repository.findAll();
    }

    public Optional<CatalogBook> findById(UUID id) {
        return repository.findById(CatalogBookId.of(id));
    }

    public void remove(UUID id) {
        repository.deleteById(CatalogBookId.of(id));
    }
}
