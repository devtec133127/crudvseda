package de.demo.lending.inventory.application;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
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

    public Pair<String, String> searchBook(String query) {
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
                    return Pair.of("N/A", "N/A");
                }

                List<?> docs = (List<?>) docsObj;
                log.debug("Docs size: {}", docs.size());
                docs.stream().limit(5).forEach(d -> log.debug("Doc: {}", d));
                if (docs.isEmpty()) {
                    log.warn("Keine Bücher gefunden");
                    return Pair.of("", "");
                }

                // 3. Jedes Element sicher casten und mappen
                List<Pair<String, String>> bookInfos = docs.stream()
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
            return Pair.of("", "");
        } catch (HttpServerErrorException e) {
            log.error("OpenLibrary API Fehler: {}", e.getStatusCode(), e);
            return Pair.of("", "");
        }
    }

    private Pair<String, String> determineBook(List<Pair<String, String>> bookInfos, String searchBootTitle) {
        if (bookInfos.isEmpty()) {
            log.warn("Book title {} not found", searchBootTitle);
            return null;
        }

        bookInfos.stream().forEach(p -> log.debug("Found Book: {} with ISBN: {}", p.getSecond(), p.getFirst()));

        Optional<Pair<String, String>> firstValidBook = bookInfos.stream()
                .filter(p -> !"N/A".equals(p.getFirst()))
                .findFirst();
        firstValidBook.ifPresentOrElse(
                b -> System.out.println("Gefundenes Buch: " + b.getSecond() + " / ISBN: " + b.getFirst()),
                () -> System.out.println("Kein Buch mit gültiger ISBN gefunden")
        );

        if (firstValidBook.isEmpty()) {
            log.error("No valid book with ISBN found for title {}", searchBootTitle);
            throw new RuntimeException("No valid book with ISBN found for title " + searchBootTitle);
        }
        log.info("Buch {} vorhanden", searchBootTitle);

        return firstValidBook.get();
    }

    private Pair<String, String> mapToBook(Map doc) {
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

        return Pair.of(isbn, title);
    }
}
