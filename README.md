# Library Lending System

> Referenzprojekt zur Demonstration von Domain-Driven Design (DDD), Event-Driven Architecture (EDA)
> und Hexagonaler Architektur am Beispiel eines digitalen Bibliothekssystems.

---

## Über das Projekt

Dieses System zeigt die praktische Anwendung etablierter Architekturmuster in einem fachlich relevanten Kontext. Es dient als Lern- und Demonstrationsprojekt für Software-Architektur, nicht als produktionsreifes System.

**Kernprinzipien:**

- **Domain-Driven Design** mit expliziten Bounded Contexts und Ubiquitous Language
- **Event-Driven Architecture** mit Choreography-Pattern (kein zentraler Orchestrator)
- **Hexagonale Architektur** (Ports & Adapters) innerhalb jedes Bounded Context
- **Modulith-Deployment** – ein Artefakt, klare Designgrenzen (siehe ADR-007)

---

## Bounded Contexts

| Context | Verantwortung | Status |
|---------|---------------|--------|
| `catalog` | Buchkatalog: Suche, Aufnahme, Metadaten | Implementiert |
| `loan` | Ausleihe: Anfrage, Aktivierung, Zustandsmaschine | Implementiert |
| `inventory` | Buchbestand: Exemplare, Reservierungen | Implementiert |
| `procurement` | Externe Beschaffung über OpenLibrary API | Teilweise |
| `payment` | Gebührenabwicklung | Grundgerüst |

---

## Implementierte Szenarien

**Szenario 1 – Buch ausleihen (lokal verfügbar)**
```
POST /loans → LoanRequested → BookReserved → LoanActivated
```

**Szenario 2 – Library Catalog (Inkrement v1)**
```
GET  /catalog/search?q={query}   → OpenLibrary-Suche
POST /catalog/books              → Buch in Katalog übernehmen
GET  /catalog/books              → Katalog anzeigen
DEL  /catalog/books/{id}         → Buch entfernen
```

---

## Tech Stack

| Technologie | Version | Zweck |
|-------------|---------|-------|
| Java | 17 | Laufzeitumgebung |
| Spring Boot | 3.3.4 | Anwendungsrahmen |
| Apache Kafka | KRaft | Event Streaming (Profil `kafka`) |
| PostgreSQL | 15 | Persistenz |
| Loki + Grafana | 3.0 / 11.0 | Lokales Log-Monitoring |
| Maven | 3.8+ | Build |
| Docker Compose | – | Lokale Infrastruktur |

---

## Quick Start

### Voraussetzungen

- Java 17+, Maven 3.8+, Docker & Docker Compose

### Option A – Nur Infrastruktur (Standard-Entwicklung)

```bash
# 1. Infrastruktur starten (PostgreSQL + Kafka)
docker compose up -d

# 2. Anwendung starten
mvn spring-boot:run

# 3. Health Check
curl http://localhost:8080/actuator/health

# 4. Catalog UI
open http://localhost:8080/catalog/index.html

# 5. EDA Demo UI
open http://localhost:8080/demo/demo.html
```

### Option B – Mit Observability Stack (Loki + Grafana)

```bash
# Infrastruktur + Monitoring gemeinsam starten
docker compose -f docker-compose.observability.yml up -d

# Anwendung starten (Logs werden automatisch nach Grafana weitergeleitet)
mvn spring-boot:run

# Grafana öffnen: http://localhost:3000  (admin / admin)
```

Vollständige Anleitung: [OBSERVABILITY.md](./OBSERVABILITY.md)

---

## API-Endpunkte

### Catalog API

```bash
# Bücher über OpenLibrary suchen
curl "http://localhost:8080/catalog/search?q=Clean+Code"

# Buch in eigenen Katalog übernehmen
curl -X POST http://localhost:8080/catalog/books \
  -H "Content-Type: application/json" \
  -d '{"isbn":"9780132350884","title":"Clean Code","author":"Robert C. Martin"}'

# Katalog anzeigen
curl http://localhost:8080/catalog/books

# Buch entfernen
curl -X DELETE http://localhost:8080/catalog/books/{id}
```

### Loan API

```bash
# Ausleihe anfordern
curl -X POST http://localhost:8080/loans \
  -H "Content-Type: application/json" \
  -d '{"userId":"518aeace-387a-4a16-a0b8-b6d6fa9e8bc3","isbn":"9789353162344"}'

# Ausleihstatus abfragen
curl http://localhost:8080/{loanId}
```

---

## Event-Flow (Szenario 1)

```
POST /loans
    ↓
LoanRequested (Domain Event)
    ↓
ReserveBookHandler → BookReserved
    ↓
LoanActivationHandler → LoanActivated
```

Echtzeit-Visualisierung: http://localhost:8080/demo/demo.html

---

## Projektstruktur

```
src/main/java/de/demo/lending/
├── catalog/          ← Buchkatalog (Inkrement v1)
├── loan/             ← Ausleihe-Kontext
├── inventory/        ← Bestandsverwaltung
├── procurement/      ← Externe Beschaffung
├── payment/          ← Zahlungsabwicklung
├── read/             ← CQRS Read Model
└── common/           ← Shared Kernel (Events, Value Objects, Config)

src/main/resources/static/
├── catalog/          ← Catalog UI (search.html, manage.html, index.html)
└── demo/             ← EDA Demo UI

infra/
├── observability/    ← Loki, Promtail, Grafana Konfiguration
└── init-multi-db.sql ← PostgreSQL Initialisierung

docs/
├── architecture/
│   ├── adr/          ← Architecture Decision Records (ADR-001 bis ADR-007)
│   ├── c4-container.md
│   └── hexagonal-catalog.md
├── ink/v1/           ← Produktinkrement v1 Dokumentation
└── observability.md  ← Observability Architekturdokumentation
```

---

## Architekturentscheidungen (ADRs)

| ADR | Entscheidung |
|-----|-------------|
| [ADR-001](docs/architecture/adr/adr-001-domain-driven-design.md) | Domain-Driven Design mit Bounded Contexts |
| [ADR-002](docs/architecture/adr/adr-002-event-driven-architecture.md) | Event-Driven Architecture mit Apache Kafka |
| [ADR-003](docs/architecture/adr/adr-003-open-library-anti-corruption-layer.md) | Anti-Corruption Layer für OpenLibrary |
| [ADR-004](docs/architecture/adr/adr-004-ai-enrichment-service.md) | AI Enrichment Service als eigenständiger Service |
| [ADR-005](docs/architecture/adr/adr-005-observability-loki-grafana.md) | Observability mit Loki und Grafana |
| [ADR-006](docs/architecture/adr/adr-006-kubernetes-deployment.md) | Kubernetes als Zielbetriebsmodell |
| [ADR-007](docs/architecture/adr/adr-007-deployment-monolith.md) | Modulith-Deployment statt Microservice pro Bounded Context |

---

## Spring Profile

| Profil | Beschreibung | Verwendung |
|--------|-------------|-----------|
| `async` (Standard) | In-Memory Events via `@Async` | Lokale Entwicklung ohne Kafka |
| `kafka` | Apache Kafka als Message Broker | Vollständiger EDA-Stack |

```bash
# Mit Kafka-Profil starten
mvn spring-boot:run -Dspring.profiles.active=kafka
```

---

## Status

Work in Progress – Referenzprojekt für Softwarearchitektur und Cloud-native Muster.
