# Produktinkrement v1 – Analyse

> **Ziel:** Library System Catalog – Suche, Detailansicht, Übernahme in Katalog, Verwaltungsmaske

---

## 1. Aktueller Zustand

### Was vorhanden ist

Das Projekt enthält bereits einen `OpenLibraryClientAdapter` im `procurement`-Kontext. Dieser sucht Bücher per ISBN über `https://openlibrary.org/search.json`. Er ist jedoch ausschließlich für den internen Beschaffungsfluss gebaut – ohne direkte REST-Exposition nach außen, ohne Autoren-/Cover-Daten, und mit einem hartcodierten Limit von 1 Ergebnis.

Eine Katalog-Konzept existiert nicht. Es gibt weder ein `CatalogBook`-Domänenobjekt noch eine Tabelle, eine API oder eine UI dafür. Das Frontend besteht aus einer einzigen Demo-Seite für den Loan-Flow.

### Was fehlt

| Feature | Backend | Frontend |
|---------|---------|----------|
| Buchsuche über OpenLibrary | `ProcurementClient` intern vorhanden, nicht exponiert | Nicht vorhanden |
| Suchergebnisse mit Autor, Cover | Nicht vorhanden (nur `title`, `isbn`, `key`) | Nicht vorhanden |
| Buchdetailansicht | Nicht vorhanden | Nicht vorhanden |
| Buch in Katalog übernehmen | Kein Katalog-Domain-Modell | Nicht vorhanden |
| Katalogverwaltung | Kein Katalog-Domain-Modell | Nicht vorhanden |

---

## 2. Vorhandene Komponenten

### Backend

| Komponente | Pfad | Relevanz für v1 |
|------------|------|-----------------|
| `OpenLibraryClientAdapter` | `procurement/adapters/out/external/` | Wiederverwendbar – Suche per ISBN |
| `ProcurementClient` (Port) | `procurement/application/ports/out/` | Interface für externen API-Aufruf |
| `SupplierInfo` (Record) | `procurement/domain/` | Enthält `title`, `isbn`, `externalBookId` – erweiterbar |
| `Isbn` (Value Object) | `common/valueobjects/` | Wiederverwendbar für Katalog |
| `BookId` (Value Object) | `common/valueobjects/` | Wiederverwendbar für Katalog |
| `LoanController` | `loan/adapters/in/rest/` | Referenz für Controller-Muster |
| `LoanStatusQueryController` | `read/adapters/in/rest/` | Referenz für Query-Controller |
| `RestTemplate` Bean | `common/config/` | Bereits konfiguriert, nutzbar |

### Frontend

| Datei | Pfad | Inhalt |
|-------|------|--------|
| `demo.html` | `static/demo/` | ISBN-Eingabe + Loan-Flow Demo |
| `demo.js` | `static/demo/` | SSE-Client, Event-Rendering (~700 Zeilen) |
| `demo.css` | `static/demo/` | Card-, Timeline-, Button-Styles |

Das CSS enthält wiederverwendbare Basisstyles (CSS-Variablen, Container, Cards, Buttons).

---

## 3. Minimale Änderungen

### Prinzip: Kein neues Framework, keine neue Architektur

Das bestehende Muster wird 1:1 übernommen:
- Neuer `catalog`-Package neben `loan`, `inventory`, `procurement`
- Gleiche Schichtenstruktur: `domain` → `application` → `adapters`
- Neue statische HTML-Seiten neben `demo.html`
- `OpenLibraryClientAdapter` wird erweitert (Autor, Cover, mehrere Ergebnisse)

### Neue Serverkomponenten (Backend)

```
catalog/
├── domain/
│   └── CatalogBook.java           ← Neues Domänenobjekt (isbn, title, author, coverUrl, addedAt)
├── application/
│   ├── ports/
│   │   ├── in/  SearchBooksUseCase, AddBookUseCase, ListCatalogUseCase
│   │   └── out/ CatalogBookRepository, LibrarySearchClient (neuer Port)
│   └── SearchBooksService, AddBookService, ListCatalogService
└── adapters/
    ├── in/rest/
    │   └── CatalogController.java  ← Neue Endpunkte
    └── out/
        ├── persistence/            ← JPA Entity + Repository
        └── external/               ← Erweitert OpenLibraryClientAdapter (oder delegiert)
```

**Erweiterungen am OpenLibraryClientAdapter:**
- Suchfeld von `isbn` auf freien Text (`q=`) erweitern
- Felder um `author_name`, `cover_i` ergänzen
- Limit von 1 auf 10 erhöhen
- `Thread.sleep(3000)` aus `orderBook()` entfernen

### Neue API-Endpunkte

| Methode | Pfad | Zweck |
|---------|------|-------|
| `GET` | `/catalog/search?q={query}` | OpenLibrary durchsuchen |
| `GET` | `/catalog/books` | Eigenen Katalog auflisten |
| `GET` | `/catalog/books/{id}` | Buchdetail aus eigenem Katalog |
| `POST` | `/catalog/books` | Buch in Katalog übernehmen |
| `DELETE` | `/catalog/books/{id}` | Buch aus Katalog entfernen |

### Neue Frontend-Seiten

| Seite | Datei | Inhalt |
|-------|-------|--------|
| Buchsuche | `static/catalog/search.html` | Suchformular (Titel/Autor/ISBN), Ergebnisliste, Detailansicht |
| Katalogverwaltung | `static/catalog/manage.html` | Liste übernommener Bücher, Entfernen |
| Shared JS/CSS | `static/catalog/catalog.js/css` | API-Calls, Rendering |

Navigation zwischen den zwei Seiten über einfache Links.

### Was explizit nicht geändert wird

- Der `loan`-, `inventory`-, `payment`-Kontext bleibt unangetastet
- Die bestehende `demo.html` bleibt unverändert
- Kein Kafka-Event für Katalogaktionen (Catalog-Operationen sind synchron/REST)
- Keine Authentifizierung / kein Multi-User-Kontext
- Kein Pagination (einfache Liste reicht für v1)
