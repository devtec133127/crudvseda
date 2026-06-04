# Hexagonal Architecture – Catalog Context

> **Level 3 – Component Diagram** für den `catalog`-Bounded-Context.  
> Zeigt das Hexagonale Architekturmuster anhand der vollständig implementierten Klassen aus Inkrement v1.

---

```mermaid
flowchart LR
    classDef external  fill:#f3f4f6,stroke:#9ca3af,color:#374151
    classDef adapter   fill:#dbeafe,stroke:#3b82f6,color:#1e3a5f
    classDef service   fill:#ede9fe,stroke:#7c3aed,color:#3b0764
    classDef port      fill:#fef3c7,stroke:#f59e0b,color:#713f12
    classDef domain    fill:#dcfce7,stroke:#16a34a,color:#14532d
    classDef infra     fill:#e0f2fe,stroke:#0284c7,color:#0c4a6e

    %% ── Externe Clients ──────────────────────────────────────────
    Browser(["Browser\nHTTP-Client"]):::external

    %% ── Inbound Adapter ──────────────────────────────────────────
    subgraph inbound["Inbound Adapter — adapters/in/rest/"]
        Controller["CatalogController\n@RestController\n/catalog/**"]:::adapter
        ReqDTO["AddBookRequest\nCatalogBookDto\n(Request / Response DTOs)"]:::adapter
    end

    %% ── Application Core ─────────────────────────────────────────
    subgraph core["Application Core — application/  +  domain/"]
        CService["CatalogService\n@Service"]:::service

        subgraph domain["Domain — domain/"]
            CBook["CatalogBook\nadd()  restore()"]:::domain
            CBookId["CatalogBookId\nnewId()  of(UUID)"]:::domain
        end

        BSResult["BookSearchResult\n(Application-DTO)"]:::domain

        subgraph ports["Output Ports — application/ports/out/"]
            RepoPort["CatalogBookRepository\nsave  findById  findAll  deleteById"]:::port
            SearchPort["LibrarySearchClient\nsearch(query)"]:::port
        end
    end

    %% ── Outbound Adapters ────────────────────────────────────────
    subgraph outbound["Outbound Adapters — adapters/out/"]
        RepoAdapter["CatalogBookRepositoryAdapter\n@Component\nimplements CatalogBookRepository"]:::adapter
        SpringJpa["SpringCatalogBookRepository\nextends JpaRepository(UUID)"]:::infra
        Entity["CatalogBookEntity\n@Entity  catalog_book"]:::infra
        SearchAdapter["OpenLibraryCatalogAdapter\n@Component\nimplements LibrarySearchClient"]:::adapter
    end

    %% ── Externe Systeme ──────────────────────────────────────────
    PostgreSQL[("PostgreSQL\ncatalog_book")]:::external
    OpenLib(["OpenLibrary API\nopenlibrary.org"]):::external

    %% ── Beziehungen: Inbound-Seite ───────────────────────────────
    Browser     -->|"HTTP / JSON"| Controller
    Controller  -->|"verwendet"| ReqDTO
    Controller  -->|"ruft auf"| CService

    %% ── Beziehungen: Application Core ───────────────────────────
    CService    ---  CBook
    CService    ---  CBookId
    CService    ---  BSResult
    CService    -->|"uses"| RepoPort
    CService    -->|"uses"| SearchPort

    %% ── Beziehungen: Outbound-Seite (Dependency Inversion) ───────
    RepoAdapter   -.->|"implements"| RepoPort
    SearchAdapter -.->|"implements"| SearchPort

    RepoAdapter   -->  Entity
    RepoAdapter   -->  SpringJpa
    SpringJpa     -->|"JPA / SQL"| PostgreSQL
    SearchAdapter -->|"HTTPS / REST"| OpenLib
```

---

## Architekturprinzipien im Diagramm

### Dependency Rule

Alle Pfeile zeigen **von außen nach innen** – die Domain-Objekte haben keine Abhängigkeit  
auf Adapter, Spring oder Datenbank. Einzige Ausnahme: die `-.->` (gestrichelte) Linie  
zeigt, dass der Adapter den Port *implementiert* (Dependency Inversion).

```
Browser → Adapter → Service → Domain
                 ↘           ↗
               Port (Interface)
                 ↗
            Adapter (implementiert Port, hängt davon ab)
```

### Farbkodierung

| Farbe | Bedeutung | Beispiele |
|-------|-----------|-----------|
| Blau | Adapter (Inbound + Outbound) | `CatalogController`, `CatalogBookRepositoryAdapter` |
| Lila | Application Service | `CatalogService` |
| Gelb | Output Ports (Interfaces) | `CatalogBookRepository`, `LibrarySearchClient` |
| Grün | Domain + Application-DTOs | `CatalogBook`, `CatalogBookId`, `BookSearchResult` |
| Hellblau | Infrastruktur-Klassen | `SpringCatalogBookRepository`, `CatalogBookEntity` |
| Grau | Externe Systeme | Browser, PostgreSQL, OpenLibrary API |

### Paketstruktur

```
de.demo.lending.catalog/
├── adapters/
│   ├── in/
│   │   └── rest/
│   │       ├── CatalogController.java          ← Blau (Inbound Adapter)
│   │       └── dto/
│   │           ├── AddBookRequest.java          ← Blau (DTO)
│   │           └── CatalogBookDto.java          ← Blau (DTO)
│   └── out/
│       ├── external/
│       │   └── OpenLibraryCatalogAdapter.java  ← Blau (Outbound Adapter)
│       └── persistence/
│           ├── CatalogBookRepositoryAdapter.java ← Blau (Outbound Adapter)
│           ├── CatalogBookEntity.java           ← Hellblau (Infrastruktur)
│           └── SpringCatalogBookRepository.java ← Hellblau (Infrastruktur)
├── application/
│   ├── BookSearchResult.java                   ← Grün (Application-DTO)
│   ├── CatalogService.java                     ← Lila (Application Service)
│   └── ports/
│       └── out/
│           ├── CatalogBookRepository.java      ← Gelb (Output Port)
│           └── LibrarySearchClient.java        ← Gelb (Output Port)
└── domain/
    ├── CatalogBook.java                        ← Grün (Domain)
    └── CatalogBookId.java                      ← Grün (Domain)
```

### Hinweis: Kein Input-Port-Interface

Der `CatalogController` ruft `CatalogService` direkt auf – es gibt kein separates  
Input-Port-Interface (wie z. B. `AddBookUseCase`). Das ist eine bewusste Vereinfachung  
für dieses Referenzprojekt. In einem größeren System würden Input-Ports die  
Testbarkeit des Controllers ohne Spring-Kontext verbessern.
