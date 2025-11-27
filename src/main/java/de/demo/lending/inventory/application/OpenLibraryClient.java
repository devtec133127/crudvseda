package de.demo.lending.inventory.application;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class OpenLibraryClient {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl = "https://openlibrary.org";

    public OpenLibraryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ExternalBookInfo searchBook(String query) {
        String url = apiBaseUrl + "/search.json?title=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&limit=1&fields=title,isbn";
        try {
            log.info("Synchroner REST-Call zu Payment-Service | Endpoint: {}", url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            log.info("Inventory-Service antwortete: Status {}", response.getStatusCode());

            log.debug("Body of Response: {}", response.getBody().toString());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object docsObj = response.getBody().get("docs");
                if (!(docsObj instanceof List)) {
                    log.warn("Keine Dokumente gefunden");
                    return new ExternalBookInfo("N/A", "N/A");
                }

                List<?> docs = (List<?>) docsObj;
                log.debug("Docs size: {}", docs.size());
                docs.stream().limit(5).forEach(d -> log.debug("Doc: {}", d));
                if (docs.isEmpty()) {
                    log.warn("Keine Bücher gefunden");
                    return new ExternalBookInfo("", "");
                }

                // 3. Jedes Element sicher casten und mappen
                List<ExternalBookInfo> bookInfos = docs.stream()
                        .filter(o -> o instanceof Map)
                        .map(o -> mapToBook((Map<String, Object>) o))
                        .collect(Collectors.toList());

                if (bookInfos.isEmpty()) {
                    log.warn("Keine Bücher nach Mapping gefunden");
                }


                return determineBook(bookInfos, query);
            } else {
                log.debug("Error: {}", response.getBody().toString());
            }
            log.debug("Search returned {} documents", response.getBody().size());
            return new ExternalBookInfo("", "");
        } catch (HttpServerErrorException e) {
            log.error("OpenLibrary API Fehler: {}", e.getStatusCode(), e);
            return new ExternalBookInfo("", "");
        }
    }

    private ExternalBookInfo determineBook(List<ExternalBookInfo> bookInfos, String searchBootTitle) {
        if (bookInfos.isEmpty()) {
            log.warn("Book title {} not found", searchBootTitle);
            return null;
        }

        bookInfos.stream().forEach(p -> log.debug("Found Book: {} with ISBN: {}", p.getTitle(), p.getIsbn()));

        Optional<ExternalBookInfo> firstValidBook = bookInfos.stream()
                .filter(p -> !"N/A".equals(p.getIsbn()))
                .findFirst();
        firstValidBook.ifPresentOrElse(
                b -> System.out.println("Gefundenes Buch: " + b.getTitle() + " / ISBN: " + b.getIsbn()),
                () -> System.out.println("Kein Buch mit gültiger ISBN gefunden")
        );

        if (firstValidBook.isEmpty()) {
            log.error("No valid book with ISBN found for title {}", searchBootTitle);
            throw new RuntimeException("No valid book with ISBN found for title " + searchBootTitle);
        }
        log.info("Buch {} vorhanden", searchBootTitle);

        return firstValidBook.get();
    }

    private ExternalBookInfo mapToBook(Map doc) {
        String title = (String) doc.get("title");
        String isbn = "N/A";

        //book.setAuthor((String) ((List) doc.get("author_name")).get(0)); // Erster Autor
        Object isbnObj = doc.get("isbn");
        if (isbnObj instanceof List<?> isbnList && !isbnList.isEmpty()) {
            isbn = (String) isbnList.get(0); // Erstes ISBN
        } else {
            // Fallback: erstes Element aus "ia", falls vorhanden
            Object iaObj = doc.get("ia");
            if (iaObj instanceof List<?> iaList && !iaList.isEmpty()) {
                String rawIa = (String) iaList.get(0);
                // Falls es mit "isbn_" beginnt, entfernen
                if (rawIa.startsWith("isbn_")) {
                    isbn = rawIa.substring(5);
                } else {
                    isbn = rawIa;
                }
            } else {
                isbn = "N/A"; // Kein Identifier verfügbar
            }
        }

        log.debug("Book mapped {}", isbn);

        return new ExternalBookInfo(isbn, title);
    }

    public class ExternalBookInfo {
        private String title;
        private String isbn;

        public ExternalBookInfo(String title, String isbn) {
            this.title = title;
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public String getIsbn() {
            return isbn;
        }
    }
}
