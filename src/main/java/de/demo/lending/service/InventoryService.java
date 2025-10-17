package de.demo.lending.service;

import de.demo.lending.domain.Book;
import de.demo.lending.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InventoryService {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl = "https://openlibrary.org";

    @Autowired
    private BookRepository bookRepository;

    public InventoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Book> searchBook(String query) {
        String url = apiBaseUrl + "/search.json?title=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&limit=1";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            log.debug("Body of Response: {}", response.getBody().toString());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object docsObj = response.getBody().get("docs");
                if (!(docsObj instanceof List)) {
                    log.warn("Keine Dokumente gefunden");
                    return Collections.emptyList();
                }

                List<?> docs = (List<?>) docsObj;
                log.debug("Docs size: {}", docs.size());
                docs.stream().limit(5).forEach(d -> log.debug("Doc: {}", d));
                if (docs.isEmpty()) {
                    log.warn("Keine Bücher gefunden");
                    return Collections.emptyList();
                }

                // 3. Jedes Element sicher casten und mappen
                List<Book> books = docs.stream()
                        .filter(o -> o instanceof Map)
                        .map(o -> mapToBook((Map<String,Object>) o))
                        .collect(Collectors.toList());

                if (books.isEmpty()) {
                    log.warn("Keine Bücher nach Mapping gefunden");
                }

                return books;
            } else {
                log.debug("Error: {}", response.getBody().toString());
            }
            log.debug("Search returned {} documents", response.getBody().size());
            return Collections.emptyList(); // Fallback für Fehler
        } catch (HttpServerErrorException e) {
            log.error("OpenLibrary API Fehler: {}", e.getStatusCode(), e);
            return Collections.emptyList();
        }
    }

    private Book mapToBook(Map doc) {
        Book book = new Book();
        book.setTitle((String) doc.get("title"));
        //book.setAuthor((String) ((List) doc.get("author_name")).get(0)); // Erster Autor
        Object isbnObj = doc.get("isbn");
        if (isbnObj instanceof List<?> isbnList && !isbnList.isEmpty()) {
            book.setIsbn((String) isbnList.get(0)); // Erstes ISBN
        } else {
            // Fallback: erstes Element aus "ia", falls vorhanden
            Object iaObj = doc.get("ia");
            if (iaObj instanceof List<?> iaList && !iaList.isEmpty()) {
                String rawIa = (String) iaList.get(0);
                // Falls es mit "isbn_" beginnt, entfernen
                if (rawIa.startsWith("isbn_")) {
                    book.setIsbn(rawIa.substring(5));
                } else {
                    book.setIsbn(rawIa);
                }
            } else {
                book.setIsbn("N/A"); // Kein Identifier verfügbar
            }
        }

        //book.setIsbn((String) doc.get("isbn"));
        book.setAvailable(true);
        log.debug("Book mapped {}", book.getIsbn());

        return book;
    }

    // Prüfe Verfügbarkeit eines Buches
    public boolean checkAvailability(String bookTitle) {
        List<Book> foundBooks = searchBook(bookTitle);
        if(foundBooks.isEmpty()) {
            log.warn("Book title {} not found", bookTitle);
            return false;
        }

        log.debug("Saving found Book with ISBN: {}", foundBooks.get(0).getIsbn());
        bookRepository.save(foundBooks.get(0));
        return true;

        //Optional<Book> book = bookRepository.findByTitle(bookTitle);
        //return book.map(b -> b.isAvailable()).orElse(false);
    }

    // Hole verfügbare Bücher für Bestandsabfrage
    public Iterable<Book> getAvailableInventory() {
        return bookRepository.findAllByAvailable(true);
    }

    // Aktualisiere Bestand nach Ausleihe (intern)
    public void updateStock(String bookTitle, boolean available) {
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(bookTitle);
        if(!books.isEmpty()) {
            Book book = books.get(0);
            book.setAvailable(available);
            bookRepository.save(book);
        }
    }

    // Prüfe User-Bestand
    public Iterable<Book> getUserStock(UUID userId) {
        return bookRepository.findByUserId(userId);
    }
}