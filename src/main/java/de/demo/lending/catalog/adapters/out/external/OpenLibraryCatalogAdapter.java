package de.demo.lending.catalog.adapters.out.external;

import de.demo.lending.catalog.application.BookSearchResult;
import de.demo.lending.catalog.application.ports.out.LibrarySearchClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenLibraryCatalogAdapter implements LibrarySearchClient {

    private static final String BASE_URL = "https://openlibrary.org";
    private static final String COVER_BASE_URL = "https://covers.openlibrary.org/b/id/";

    private final RestTemplate restTemplate;

    public OpenLibraryCatalogAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<BookSearchResult> search(String query) {
        String url = BASE_URL + "/search.json?q="
                + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&limit=10&fields=title,author_name,isbn,cover_i,key";

        log.info("OpenLibrary-Katalogsuche | Query: {} | URL: {}", query, url);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                log.warn("OpenLibrary antwortete mit leerem Body für Query: {}", query);
                return Collections.emptyList();
            }

            Object docsObj = response.get("docs");
            if (!(docsObj instanceof List<?> docs)) {
                log.warn("Kein 'docs'-Feld in OpenLibrary-Antwort für Query: {}", query);
                return Collections.emptyList();
            }

            List<BookSearchResult> results = docs.stream()
                    .filter(o -> o instanceof Map)
                    .map(o -> mapToResult((Map<?, ?>) o))
                    .filter(r -> r != null && r.title() != null && !r.title().isBlank())
                    .toList();

            log.info("OpenLibrary lieferte {} Treffer für Query: {}", results.size(), query);
            return results;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("OpenLibrary HTTP-Fehler bei Query '{}': {}", query, e.getStatusCode());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unerwarteter Fehler bei OpenLibrary-Suche für Query '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private BookSearchResult mapToResult(Map<?, ?> doc) {
        String title = getStringOrDefault(doc, "title", "Unbekannter Titel");
        String author = getFirstListElement(doc, "author_name");
        String isbn = getFirstListElement(doc, "isbn");
        String coverUrl = buildCoverUrl(doc);
        String openLibraryKey = getStringOrNull(doc, "key");

        return new BookSearchResult(isbn, title, author, coverUrl, openLibraryKey);
    }

    private String getStringOrDefault(Map<?, ?> doc, String key, String defaultValue) {
        Object value = doc.get(key);
        return value instanceof String s ? s : defaultValue;
    }

    private String getStringOrNull(Map<?, ?> doc, String key) {
        Object value = doc.get(key);
        return value instanceof String s ? s : null;
    }

    private String getFirstListElement(Map<?, ?> doc, String key) {
        Object value = doc.get(key);
        if (value instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof String s) {
            return s;
        }
        return null;
    }

    private String buildCoverUrl(Map<?, ?> doc) {
        Object coverId = doc.get("cover_i");
        if (coverId instanceof Number n) {
            return COVER_BASE_URL + n.intValue() + "-M.jpg";
        }
        return null;
    }
}
