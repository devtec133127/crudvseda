# Projektanalyse: Lending System

> **Stand:** Juni 2026 | **Branch:** `eda` | **Zweck:** Referenzprojekt für DDD + Event-Driven Architecture

---

## Ziel dieser Analyse

Das Projekt soll als Referenzimplementierung dienen und von einem einfachen Demo-Zustand zu einem professionellen **Library System Catalog** ausgebaut werden. Später soll ein separater **AI Enrichment Service** ergänzt werden. Es werden maximal **2–3 sinnvolle Hauptszenarien** umgesetzt.

---

## 1. Aktuelle Architektur

Das Projekt implementiert drei Architekturprinzipien konsequent:

### Domain-Driven Design (DDD)
Vier Bounded Contexts, klar voneinander getrennt:

| Bounded Context | Aufgabe | Status |
|-----------------|---------|--------|
| `loan` | Ausleihverwaltung (Kern-Domain) | Implementiert ✅ |
| `inventory` | Buchbestandsverwaltung | Teilweise ✅ |
| `procurement` | Beschaffung über OpenLibrary | Teilweise ✅ |
| `payment` | Gebührenerhebung | Nur Grundgerüst ⚠️ |
| `read` | CQRS Read Model für Statusabfragen | Implementiert ✅ |

### Hexagonale Architektur (Ports & Adapters)
Jeder Bounded Context ist intern nach dem gleichen Muster aufgebaut:

```
loan/
├── adapters/
│   ├── in/
│   │   ├── rest/          → REST-Controller (primäre Adapter)
│   │   ├── messaging/     → Kafka/Async Event Handler (primäre Adapter)
│   │   └── demo/          → Demo-Controller mit SSE
│   └── out/
│       ├── persistence/   → JPA Repository Adapter (sekundäre Adapter)
│       └── external/      → Externe API-Aufrufe (sekundäre Adapter)
├── application/           → Use Cases / Application Services
│   ├── ports/             → Input/Output Ports (Interfaces)
│   └── services/          → Implementierung der Use Cases
└── domain/                → Aggregate Roots, Value Objects, Domain Events
```

### Event-Driven Architecture (EDA)
Choreography-basiertes Eventsystem – kein zentraler Orchestrator:

```
POST /loans
    ↓
LoanRequested (Domain Event)
    ↓
ReserveBookHandler → BookReserved ODER BookNotFoundLocally
    ↓
LoanActivationHandler → LoanActivated
```

**Event-Infrastruktur:**
- Profil `async`: Spring `@Async` + In-Memory (Standard)
- Profil `kafka`: Apache Kafka mit KRaft-Mode

**Implementiertes Haupt-Szenario (Szenario 1):**
ISBN liegt lokal vor → Ausleihe wird direkt aktiviert

**Teilweise implementiert (Szenario 2):**
ISBN nicht lokal → Beschaffung über OpenLibrary → ProcurementOrder

---

## 2. Verwendete Technologien

### Core
| Technologie | Version | Zweck |
|-------------|---------|-------|
| Java | 17 | Laufzeitumgebung |
| Spring Boot | 3.3.4 | Anwendungsrahmen |
| Maven | 3.8+ | Build-System |

### Persistenz
| Technologie | Version | Zweck |
|-------------|---------|-------|
| PostgreSQL | 15 | Primäre Datenbank |
| Spring Data JPA | (via Boot) | ORM-Layer |
| Hibernate | (via Boot) | DDL-Generierung (auto-create) |

### Messaging
| Technologie | Version | Zweck |
|-------------|---------|-------|
| Apache Kafka | KRaft | Event Streaming |
| Spring Kafka | (via Boot) | Kafka-Integration |

### Bibliotheken
| Bibliothek | Version | Zweck |
|------------|---------|-------|
| Lombok | (via Boot) | Code-Generierung |
| Jackson | (via Boot) | JSON-Serialisierung |
| Idempotence4j | 1.7.1 | Idempotente Event-Verarbeitung |
| ArchUnit | 1.2.0 | Architektur-Tests (deklariert, nicht genutzt) |

### Testing
| Technologie | Version | Zweck |
|-------------|---------|-------|
| JUnit 5 | (via Boot) | Test-Framework |
| Testcontainers | 1.20.1 | PostgreSQL + Kafka in Tests |
| Spring Boot Test | (via Boot) | Integrationstests |

### Frontend
- Vanilla JavaScript (kein Framework)
- Server-Sent Events (SSE) für Echtzeit-Updates

### Externe APIs
- OpenLibrary (`https://openlibrary.org/search.json`) für Buchsuche

---

## 3. Startanleitung

### Voraussetzungen
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Start

```bash
# 1. Infrastruktur starten (PostgreSQL + Kafka)
docker-compose up -d

# 2. Anwendung bauen und starten
mvn spring-boot:run

# 3. Health Check
curl http://localhost:8080/actuator/health

# 4. Demo-UI
open http://localhost:8080/demo/demo.html
```

### Profile
```bash
# Standard: Async/In-Memory Events
mvn spring-boot:run

# Mit Kafka
mvn spring-boot:run -Dspring.profiles.active=kafka
```

### Docker Compose Services
| Service | Port | Datenbank |
|---------|------|-----------|
| PostgreSQL | 5432 | `loans`, `inventory`, `accounting`, `reporting` |
| Kafka | 9092 | KRaft-Mode (kein ZooKeeper) |

---

## 4. Bestehende Domänenmodelle

### Value Objects (Shared Kernel)

| Value Object | Inhalt |
|--------------|--------|
| `UserId` | Benutzer-Identifier |
| `LoanId` | Ausleihe-Identifier (UUID) |
| `BookId` | Buch-Identifier |
| `CopyId` | Exemplar-Identifier |
| `Isbn` | ISBN-10 / ISBN-13 mit Validierung und Normalisierung |
| `BookTitle` | Buchtitel (Grundgerüst, leer) |

### Aggregate Roots

**`Loan` (loan/domain/Loan.java)**
```
Status-Maschine: REQUESTED → READY_FOR_PICKUP → ACTIVE → CLOSED

Felder: userId, isbn, copyId, dueDate
Events: LoanRequested, LoanActivated
Methoden: request(), activate(), markAsReadyForPickup(), checkOut()
```

**`InventoryCopy` (inventory/domain/InventoryCopy.java)**
```
Status-Maschine: IN_TRANSIENT → REGISTERED → AVAILABLE → RESERVED → LOANED

Felder: bookId, isbn, loanId, reservationId
Events: BookReserved, BookRegistered
Methoden: reserve(), registerBook(), markAsAvailable()
```

**`ProcurementOrder` (procurement/domain/ProcurementOrder.java)**
```
Status-Maschine: INITIATED → ORDERED → IN_TRANSIT → RECEIVED → COMPLETED

Felder: loanId, isbn, bookId
Events: ProcurementInitiated, BookOrderedExternally, BookReceived
Hinweis: markInTransit(), complete(), cancel() sind auskommentiert
```

**`Payment` (payment/domain/Payment.java)**
```
Status-Maschine: Nur INITIATED implementiert (capture, fail, refund auskommentiert)

Felder: loanId, userId, amount (Money), method (PaymentMethod)
Events: PaymentInitiated
```

### Read Model (CQRS)

**`LoanStatusRead`**
```
Felder: loanId, customerId, loanStatus, paymentStatus, fee, paymentPaidAt, updatedAt
Zweck: Denormalisiertes Lesemodell für Statusabfragen
```

---

## 5. Aktuelle API-Endpunkte

### Write Side (Commands)

| Methode | Pfad | Body | Rückgabe | Beschreibung |
|---------|------|------|----------|--------------|
| `POST` | `/loans` | `{ userId, isbn }` | `{ loanId }` | Neue Ausleihe erstellen |
| `POST` | `/api/demo/loans` | `{ userId, isbn }` | `DemoLoanResponse` | Demo-Endpunkt |

### Read Side (Queries)

| Methode | Pfad | Rückgabe | Beschreibung |
|---------|------|----------|--------------|
| `GET` | `/{loanId}` | `LoanStatusRead` | Ausleih-Status abrufen |
| `GET` | `/customer/{customerId}` | `List<LoanStatusRead>` | Alle Ausleihen eines Nutzers |

### SSE (Server-Sent Events)

| Methode | Pfad | Rückgabe | Beschreibung |
|---------|------|----------|--------------|
| `GET` | `/api/demo/loans/{loanId}/events` | `text/event-stream` | Echtzeit-Event-Stream |

### Fehlende Endpunkte (Lücken)
- Kein `/books` oder `/catalog` Endpunkt
- Kein Endpunkt für Buchrückgabe
- Kein Endpunkt zur Inventarverwaltung
- Keine Admin-Endpunkte

---

## 6. Frontend-Struktur

**Pfad:** `src/main/resources/static/demo/`

| Datei | Größe | Inhalt |
|-------|-------|--------|
| `demo.html` | ~ 50 Zeilen | Formular mit ISBN-Eingabe und Event-Timeline |
| `demo.js` | ~ 700 Zeilen | SSE-Client, Event-Rendering, Status-Anzeige |
| `demo.css` | Styling | Cards, Timeline, Buttons |

**Technologie:** Vanilla JavaScript, Server-Sent Events

**Funktion:**
1. Benutzer gibt ISBN ein (Default: `9789353162344`)
2. POST an `/api/demo/loans` → erhält `loanId`
3. SSE-Verbindung zu `/api/demo/loans/{loanId}/events`
4. Domain Events werden in einer Timeline dargestellt
5. Finaler Status wird angezeigt

**Limitierungen:**
- Nur für Demo-Zwecke ausgelegt
- Keine Fehlerbehandlung für Netzwerkprobleme
- Kein State Management
- Hard-codierte ISBN als Default

---

## 7. Datenbankzugriffe

### Schema-Übersicht

| Tabelle | Entity | Bounded Context |
|---------|--------|-----------------|
| `loan` | `LoanEntity` | loan |
| `inventory_copy` | `InventoryCopyEntity` | inventory |
| `reservation` | `ReservationEntity` | inventory |
| `pending_reservation` | `PendingReservationEntity` | inventory |
| `payment` | `PaymentEntity` | payment |
| `procurement_order` | `ProcurementOrderEntity` | procurement |
| `loan_status_read` | `LoanStatusRead` | read (CQRS) |
| `processed_event` | `ProcessedEventEntity` | common (Idempotenz-Inbox) |
| `outbox` | `OutboxEntity` | common (deaktiviert) |

### Repository-Muster (Hexagonale Architektur)

```
Domain Port (Interface)           Adapter (Implementierung)          Spring JPA
──────────────────────────────────────────────────────────────────────────────
LoanRepository               → LoanJpaRepositoryAdapter          → SpringLoanJpaRepository
InventoryRepository          → InventoryRepositoryAdapter         → SpringInventoryCopyRepository
ReservationRepository        → ReservationRepositoryAdapter       → SpringReservationRepository
ProcurementOrderRepository   → ProcurementOrderRepositoryAdapter  → SpringProcurementOrderRepository
PaymentRepository            → PaymentRepositoryAdapter           → SpringPaymentRepository
```

### Schema-Management
- **Aktuell:** Hibernate DDL `create` (Schema bei jedem Start neu erstellt – Datenverlust!)
- **Geplant:** Liquibase (deaktiviert: `spring.liquibase.enabled: false`)
- **Infrastruktur:** `infra/init-multi-db.sql` erstellt Datenbanken für Docker

### Idempotenz
- `ProcessedEventEntity` verhindert doppelte Event-Verarbeitung
- Idempotence4j-Library mit `ActionId` und `IdempotenceService`
- Outbox-Pattern vorbereitet aber deaktiviert (`outbox.enabled: false`)

---

## 8. Testabdeckung

**Aktuell: Keine Tests vorhanden.**

Das `src/test/java/` Verzeichnis ist leer.

### Vorhandene Test-Infrastruktur (deklariert, aber ungenutzt)

| Abhängigkeit | Zweck |
|--------------|-------|
| `spring-boot-starter-test` | JUnit 5, Mockito, MockMvc |
| `testcontainers` (PostgreSQL) | Echte Datenbank in Tests |
| `testcontainers` (Kafka) | Echte Kafka-Instanz in Tests |
| `archunit` | Architektur-Constraints testen |

### Empfohlene Test-Pyramide

| Ebene | Anzahl | Beispiele |
|-------|--------|-----------|
| Domänen-Unit-Tests | 30–50 | `Loan.request()`, `Isbn` Validierung, Status-Übergänge |
| Application Service Tests | 10–20 | `CreateLoan` mit Mocks |
| Architektur-Tests (ArchUnit) | 5–10 | Keine Domain-Imports in Adaptern |
| Integrationstests | 5–10 | REST-Endpoint + Datenbank (Testcontainers) |

---

## 9. Technische Schulden

### Kritisch

| Problem | Datei | Auswirkung |
|---------|-------|-----------|
| Hibernate DDL `create` | `application.yml` | Datenverlust bei jedem Neustart |
| Keine Migrations | Liquibase deaktiviert | Schema nicht versioniert |
| Keine Tests | `src/test/` leer | Kein Sicherheitsnetz für Änderungen |

### Hoch

| Problem | Datei | Details |
|---------|-------|---------|
| `Thread.sleep(2000)` | `InventoryCopy.java:95` | Simulated delay im Domänenmodell – falsche Schicht |
| `Thread.sleep(3000)` | `OpenLibraryClientAdapter.java` | Simulierter externer Aufruf – kein realer API-Call |
| Auskommentierter Code | `Payment.java:38–94` | `capture()`, `fail()`, `refund()` nicht implementiert |
| Auskommentierter Code | `ProcurementOrder.java:133–193` | `markInTransit()`, `complete()`, `cancel()` fehlen |
| Auskommentiertes Event | `Loan.java:107` | `LoanReadyForPickup` wird nie gefeuert |
| Falsches Log-Label | `OpenLibraryClientAdapter.java` | Log sagt "Payment-Service", Code ruft OpenLibrary auf |

### Mittel

| Problem | Details |
|---------|---------|
| `alternative/` Package | Veraltetes Designbeispiel (`Book` + `BookCopy`) im Produktions-Code |
| Outbox-Pattern deaktiviert | Vorbereitet aber nicht aktiv – At-Least-Once Garantie fehlt |
| Keine Timeout-Konfiguration | `RestTemplate` für OpenLibrary ohne Timeout |
| Hard-codierte ISBN im UI | `demo.html` mit fest eingetragener ISBN |
| Kein Error-Handling | `ReserveBookHandler` ohne Fehlerbehandlung für Netzwerkfehler |

### Niedrig

| Problem | Details |
|---------|---------|
| `BookTitle` leer | Value Object ohne Implementierung |
| ArchUnit-Dependency | Deklariert aber keine Tests vorhanden |
| Inkonsistente Paketnamen | `payment` nutzt `adapter` (singular), andere `adapters` (plural) |

---

## 10. Empfohlene nächste Schritte

### Kurzfristig: Stabilisierung (vor Weiterentwicklung)

1. **Hibernate DDL auf `validate` umstellen + Liquibase aktivieren**
   - Keine Datenverluste mehr bei Neustarts
   - Migrations-History für Schemaänderungen

2. **`Thread.sleep()` entfernen**
   - Aus Domänenmodell (`InventoryCopy.java`)
   - Aus Adapter (`OpenLibraryClientAdapter.java`)

3. **Auskommentierten Code bereinigen**
   - Entweder implementieren oder löschen
   - `alternative/` Package entfernen

4. **Erste Domänen-Unit-Tests schreiben**
   - `Isbn` Validierung, `Loan` Status-Übergänge
   - Einführung in den Entwicklungsworkflow

---

### Mittelfristig: Library System Catalog (Hauptziel)

Das Projekt soll von einem abstrakten Lending-Demo zu einem konkreten **Library System Catalog** werden.

#### Szenario 1 (bereits vorhanden, stabilisieren):
**Buch ausleihen – lokal verfügbar**
```
Nutzer → Ausleihe anfragen → Exemplar reservieren → Ausleihe aktiviert
```

#### Szenario 2 (ausbauen):
**Buchkatalog pflegen und abfragen**
```
Neuer Catalog-Service:
- GET /catalog/books?isbn={isbn} → Buchinformationen anzeigen
- POST /catalog/books → Buch manuell registrieren
- AI Enrichment Hook: Metadaten automatisch anreichern (Phase 3)
```
Domänenmodell: `Book` (ISBN, Titel, Autor, Beschreibung, Cover-URL, Kategorien)

#### Szenario 3 (optional):
**Buchbeschaffung – nicht lokal verfügbar**
```
Ausleihe anfragen → Buch nicht lokal → Bestellung über OpenLibrary → 
Eingang registrieren → Exemplar anlegen → Ausleihe aktivieren
```
Status: Grundfluss implementiert, Zustandsmaschine vervollständigen.

---

### Langfristig: AI Enrichment Service (separater Service)

Ein eigenständiger Microservice (separate Codebasis):

```
AI Enrichment Service
    ↓ (konsumiert)
catalog.book_registered.v1 Event
    ↓ (ruft auf)
LLM API (z. B. Claude API) oder externe Metadaten-API
    ↓ (publiziert)
catalog.book_enriched.v1 Event
    ↓ (aktualisiert)
Library Catalog → angereichertes Buchobjekt
```

**Anreicherungs-Felder:**
- Kurzbeschreibung / Zusammenfassung
- Genre / Kategorien
- Ähnliche Bücher
- Leseniveaueinstufung

---

## Zusammenfassung

| Bereich | Bewertung |
|---------|-----------|
| Architektur | Sehr gut – DDD + Hexagonal + EDA konsequent umgesetzt |
| Domain Design | Gut – klare Aggregate, Value Objects, Events |
| Implementierungstiefe | Begrenzt – nur Szenario 1 vollständig |
| Tests | Nicht vorhanden – größtes Risiko |
| Schema-Management | Kritisch – Hibernate DDL create, kein Liquibase |
| Frontend | Nur Demo – ausreichend für Referenzprojekt |
| Externe Integration | Vorhanden (OpenLibrary), aber mit simulierten Delays |
| Erweiterbarkeit | Hoch – Architektur ermöglicht einfaches Hinzufügen neuer Contexts |

Das Projekt eignet sich gut als **Referenzarchitektur** für DDD und Event-Driven Design. Vor dem Ausbau zum Library System Catalog sollten die kritischen technischen Schulden (Datenbankmigrationen, Tests) adressiert werden.
