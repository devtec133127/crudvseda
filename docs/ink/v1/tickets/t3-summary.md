# T3 – Implementierungszusammenfassung

## Neue Dateien

| Datei | Zweck |
|-------|-------|
| `catalog/adapters/in/rest/dto/AddBookRequest.java` | Request-DTO für `POST /catalog/books` |
| `catalog/adapters/in/rest/dto/CatalogBookDto.java` | Response-DTO für `GET /catalog/books` und `GET /catalog/books/{id}` |
| `catalog/application/CatalogService.java` | Application Service mit allen 5 Katalogoperationen |
| `catalog/adapters/in/rest/CatalogController.java` | REST-Controller unter `/catalog` |

Keine bestehende Datei wurde verändert.

## Getroffene Annahmen

**Abweichung vom Ticket: Service-Signatur für `add()`**

Das Ticket spezifiziert `CatalogService.add(AddBookRequest request)`. Das würde eine Abhängigkeit von der Application-Schicht auf die Adapter-Schicht erzeugen (`catalog.application` → `catalog.adapters.in.rest.dto`) – ein Verstoß gegen Hexagonale Architektur, in der Abhängigkeiten immer von außen nach innen zeigen.

Implementiert wurde stattdessen:
```java
CatalogBookId add(String isbn, String title, String author, String coverUrl, String openLibraryKey)
```

Der Controller extrahiert die Einzelfelder aus `AddBookRequest` und übergibt sie. Das Verhalten ist identisch; die Schichtengrenze bleibt sauber.

**Mapping `CatalogBook → CatalogBookDto` im Controller**

Gemäß Ticketempfehlung liegt die `toDto()`-Methode als private Methode im Controller (Präsentationsverantwortung).

**`DELETE` ohne 404-Prüfung**

Das Ticket spezifiziert kein Fehlerverhalten für das Löschen einer nicht vorhandenen ID. `JpaRepository.deleteById()` wirft in diesem Fall eine `EmptyResultDataAccessException`. Da die Fehlerbehandlung für diesen Fall nicht in scope ist, antwortet der Endpunkt bei unbekannter ID mit einem Spring-Standard-Fehler (500). Für v1 akzeptabel; kann bei Bedarf mit einer try/catch-Behandlung auf 404 umgestellt werden.

## So kann ich es testen

### 1. Kompilierung

```bash
mvn compile
```

### 2. Anwendung starten

```bash
docker-compose up -d
mvn spring-boot:run
```

### 3. OpenLibrary-Suche

```bash
curl "http://localhost:8080/catalog/search?q=Clean+Code"
```

Erwartete Antwort: JSON-Array mit Treffern, jeder mit `title`, `author`, `isbn`, `coverUrl`, `openLibraryKey`.

### 4. Buch in Katalog übernehmen

```bash
curl -s -X POST http://localhost:8080/catalog/books \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "9780132350884",
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "coverUrl": "https://covers.openlibrary.org/b/id/8432581-M.jpg",
    "openLibraryKey": "/works/OL1820750W"
  }'
```

Erwartete Antwort: `{"id":"<uuid>"}` mit HTTP 201.

### 5. Alle Katalogbücher abrufen

```bash
curl http://localhost:8080/catalog/books
```

Erwartete Antwort: JSON-Array mit dem gerade hinzugefügten Buch.

### 6. Einzelnes Buch abrufen

```bash
# <id> aus Schritt 4 verwenden
curl http://localhost:8080/catalog/books/<id>
```

Erwartete Antwort: einzelnes Buch als JSON-Objekt mit HTTP 200.

### 7. Buch löschen

```bash
curl -X DELETE http://localhost:8080/catalog/books/<id>
# HTTP 204, kein Body

curl http://localhost:8080/catalog/books/<id>
# HTTP 404
```

### 8. Validierung: leerer Titel

```bash
curl -s -X POST http://localhost:8080/catalog/books \
  -H "Content-Type: application/json" \
  -d '{"title": "", "isbn": "123"}'
```

Erwartete Antwort: `{"error":"Titel darf nicht leer sein"}` mit HTTP 400.

### 9. Bestehende Demo unverändert

```bash
curl http://localhost:8080/demo/demo.html
# HTTP 200, HTML-Seite
```
