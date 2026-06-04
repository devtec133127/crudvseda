# Backlog – Produktinkrement v1: Library System Catalog

> **Scope:** Maximal 6 Tickets. Kein neues Framework. Kein AI. Minimale Änderungen.

---

## Reihenfolge und Abhängigkeiten

```
[T1] Catalog Domain + Persistenz
        ↓
[T2] OpenLibrary-Suche erweitern      [T3] Katalog-REST-Endpunkte
        ↓                                      ↓
[T4] Frontend: Buchsuche           [T5] Frontend: Katalogverwaltung
                    ↓
             [T6] Navigations-Integration
```

T1 ist Basis für T3. T2 ist Basis für T4. T6 schließt alles zusammen.

---

## Tickets

---

### T1 · Catalog Domain und Persistenz

**Prio:** Hoch – Fundament für alle anderen Tickets

**Ziel:** Ein eigenständiger `catalog`-Bounded-Context nach bestehendem Hexagonal-Muster.

**Umfang:**

Domain-Objekt `CatalogBook` mit:
- `isbn` (Isbn Value Object, eindeutig)
- `title` (String)
- `author` (String)
- `coverUrl` (String, optional)
- `openLibraryKey` (String – Referenz zur Quelle, z. B. `/works/OL1234W`)
- `addedAt` (Instant)

JPA-Entity `CatalogBookEntity` + `SpringCatalogBookRepository` + Port-Adapter `CatalogBookRepositoryAdapter`.

Tabelle `catalog_book` wird per Hibernate DDL auto-create erzeugt (konsistent mit bestehenden Tabellen).

**Nicht enthalten:** Kein Event, kein Kafka, keine ISBN-Pflichtvalidierung im ersten Schritt.

**Akzeptanzkriterium:** `CatalogBook` kann gespeichert und per ISBN gelesen werden.

---

### T2 · OpenLibrary-Suche erweitern

**Prio:** Hoch – Basis für die Suchmaske

**Ziel:** Der bestehende `OpenLibraryClientAdapter` wird so erweitert, dass er für eine freie Suche (Titel, Autor, ISBN) geeignet ist und reichere Daten liefert.

**Umfang:**

Neuer Port `LibrarySearchClient` im `catalog`-Kontext:
```java
interface LibrarySearchClient {
    List<BookSearchResult> search(String query);
}
```

`BookSearchResult` enthält: `isbn`, `title`, `author`, `coverUrl`, `openLibraryKey`.

Entweder:
- `OpenLibraryClientAdapter` implementiert zusätzlich `LibrarySearchClient`, **oder**
- Ein neuer `OpenLibraryCatalogAdapter` im `catalog`-Kontext delegiert an denselben `RestTemplate` Bean.

API-Aufruf wird von `isbn=` auf `q=` umgestellt. Felder: `title,author_name,isbn,cover_i,key`. Limit: 10. `Thread.sleep` entfernen.

Cover-URL-Schema von OpenLibrary: `https://covers.openlibrary.org/b/id/{cover_i}-M.jpg`

**Nicht enthalten:** Kein Caching, kein Paging, kein Fehler-Retry.

**Akzeptanzkriterium:** `GET /catalog/search?q=Kafka` liefert eine JSON-Liste mit Titel, Autor, ISBN und Cover-URL.

---

### T3 · Katalog-REST-Endpunkte

**Prio:** Hoch – Basis für die Verwaltungsmaske

**Ziel:** REST-API für den eigenen Buchkatalog.

**Umfang:**

`CatalogController` unter `/catalog`:

| Methode | Pfad | Body / Response |
|---------|------|-----------------|
| `GET` | `/catalog/search?q=` | `List<BookSearchResult>` |
| `POST` | `/catalog/books` | `AddBookRequest` → `201 Created` + `{ id }` |
| `GET` | `/catalog/books` | `List<CatalogBookDto>` |
| `GET` | `/catalog/books/{id}` | `CatalogBookDto` oder `404` |
| `DELETE` | `/catalog/books/{id}` | `204 No Content` |

`AddBookRequest` enthält dieselben Felder wie `BookSearchResult` – Frontend übergibt das Suchergebnis direkt.

**Nicht enthalten:** Keine Duplikat-Prüfung per ISBN (v1-Scope), kein Paging.

**Akzeptanzkriterium:** Über curl kann ein Buch gesucht, hinzugefügt, gelistet und gelöscht werden.

---

### T4 · Frontend: Buchsuche

**Prio:** Mittel – erstes nutzbares Feature

**Ziel:** Eine eigenständige HTML-Seite für die Buchsuche über OpenLibrary.

**Pfad:** `src/main/resources/static/catalog/search.html`

**Umfang:**

Suchmaske:
- Eingabefeld (Titel, Autor oder ISBN)
- Button „Suchen"
- Trefferliste (Kacheln oder Listenzeilen): Cover-Thumbnail, Titel, Autor, ISBN
- Klick auf Treffer öffnet Detailansicht (inline, kein neues Fenster)

Detailansicht (inline aufklappbar oder einfaches Modal):
- Großes Cover, Titel, Autor, ISBN, OpenLibrary-Schlüssel
- Button „In Katalog übernehmen" → POST an `/catalog/books` → Bestätigung

Technologie: Vanilla JS (wie `demo.js`), CSS-Variablen aus `demo.css` wiederverwenden.

**Nicht enthalten:** Kein Paging, kein Autocomplete, kein Lazy Loading.

**Akzeptanzkriterium:** Im Browser kann nach einem Buch gesucht, ein Treffer inspiziert und in den Katalog übernommen werden.

---

### T5 · Frontend: Katalogverwaltung

**Prio:** Mittel

**Ziel:** Eine Verwaltungsseite für die bereits übernommenen Bücher.

**Pfad:** `src/main/resources/static/catalog/manage.html`

**Umfang:**

- Tabelle / Kartenliste der Bücher im Katalog (Cover, Titel, Autor, ISBN, Aufnahmedatum)
- Jede Zeile hat einen „Entfernen"-Button → DELETE an `/catalog/books/{id}` → Seite neu laden
- Seite lädt Daten beim Öffnen via `GET /catalog/books`
- Link zurück zur Suche

**Nicht enthalten:** Kein Sortieren, kein Filtern, kein Edit-Formular.

**Akzeptanzkriterium:** Übernommene Bücher sind sichtbar und können einzeln entfernt werden.

---

### T6 · Navigation und Landing Page

**Prio:** Niedrig – poliert das Inkrement ab

**Ziel:** Einstiegspunkt, der die zwei neuen Seiten verbindet und das Projekt als Library Catalog erkennbar macht.

**Umfang:**

Einfache `index.html` unter `static/catalog/`:
- Titel „Library System Catalog"
- Zwei Kacheln / Links: „Bücher suchen" und „Mein Katalog"
- Optional: Link zur bestehenden EDA-Demo (`/demo/demo.html`)

Anpassung der Seitentitel in `search.html` und `manage.html` (konsistente Benennung).

**Nicht enthalten:** Keine Navigation im Sinne einer SPA, kein Router.

**Akzeptanzkriterium:** `http://localhost:8080/catalog/index.html` ist der sinnvolle Startpunkt für v1.

---

## Zusammenfassung

| # | Ticket | Prio | Typ | Abhängigkeit |
|---|--------|------|-----|--------------|
| T1 | Catalog Domain + Persistenz | Hoch | Backend | – |
| T2 | OpenLibrary-Suche erweitern | Hoch | Backend | – |
| T3 | Katalog-REST-Endpunkte | Hoch | Backend | T1, T2 |
| T4 | Frontend: Buchsuche | Mittel | Frontend | T3 |
| T5 | Frontend: Katalogverwaltung | Mittel | Frontend | T3 |
| T6 | Navigation und Landing Page | Niedrig | Frontend | T4, T5 |

**Empfohlene Reihenfolge:** T1 → T2 → T3 → T4 → T5 → T6
