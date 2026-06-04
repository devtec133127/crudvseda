package de.demo.lending.catalog.adapters.in.rest;

import de.demo.lending.catalog.adapters.in.rest.dto.AddBookRequest;
import de.demo.lending.catalog.adapters.in.rest.dto.CatalogBookDto;
import de.demo.lending.catalog.application.BookSearchResult;
import de.demo.lending.catalog.application.CatalogService;
import de.demo.lending.catalog.domain.CatalogBook;
import de.demo.lending.catalog.domain.CatalogBookId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookSearchResult>> search(@RequestParam String q) {
        log.info("Katalogsuche | Query: {}", q);
        List<BookSearchResult> results = service.search(q);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/books")
    public ResponseEntity<?> add(@RequestBody AddBookRequest request) {
        log.info("Buch zum Katalog hinzufügen | Titel: {}", request.title());
        try {
            CatalogBookId id = service.add(
                    request.isbn(),
                    request.title(),
                    request.author(),
                    request.coverUrl(),
                    request.openLibraryKey()
            );
            return ResponseEntity.status(201).body(Map.of("id", id.value().toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/books")
    public ResponseEntity<List<CatalogBookDto>> findAll() {
        List<CatalogBookDto> books = service.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<CatalogBookDto> findById(@PathVariable UUID id) {
        Optional<CatalogBook> book = service.findById(id);
        return book.map(b -> ResponseEntity.ok(toDto(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {
        log.info("Buch aus Katalog entfernen | ID: {}", id);
        service.remove(id);
        return ResponseEntity.noContent().build();
    }

    private CatalogBookDto toDto(CatalogBook book) {
        return new CatalogBookDto(
                book.getId().value().toString(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getCoverUrl(),
                book.getOpenLibraryKey(),
                book.getAddedAt()
        );
    }
}
