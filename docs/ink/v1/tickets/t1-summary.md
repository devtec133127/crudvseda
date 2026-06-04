# T1 – Implementierungszusammenfassung

## Neue Dateien

| Datei | Zweck |
|-------|-------|
| `catalog/domain/CatalogBookId.java` | UUID Value Object mit `newId()` und `of(UUID)` |
| `catalog/domain/CatalogBook.java` | Domänenobjekt mit `add(...)` (neu) und `restore(...)` (aus DB) |
| `catalog/application/ports/out/CatalogBookRepository.java` | Output Port: `save`, `findById`, `findAll`, `deleteById` |
| `catalog/adapters/out/persistence/CatalogBookEntity.java` | JPA Entity, Tabelle `catalog_book` |
| `catalog/adapters/out/persistence/SpringCatalogBookRepository.java` | Spring Data JPA Interface |
| `catalog/adapters/out/persistence/CatalogBookRepositoryAdapter.java` | Adapter mit `toDomain` / `toEntity` Mapping |

Keine bestehende Datei wurde verändert.

## Getroffene Annahmen

- **`isbn` ist nullable.** OpenLibrary-Einträge haben nicht immer eine gültige ISBN-10/13. Das bestehende `Isbn`-Value-Object würde hier zu früh validieren und Einträge ohne ISBN ablehnen.
- **`restore()`-Factory-Methode** wurde neben `add()` ergänzt. Sie wird vom Adapter beim Lesen aus der DB aufgerufen und übernimmt id und addedAt unverändert aus der Entity – kein `Instant.now()` beim Laden.
- **Kein AggregateRoot, keine Domain Events.** Der `catalog`-Kontext ist in v1 synchron und REST-getrieben. Events werden ergänzt, wenn ein asynchroner AI-Enrichment-Service hinzukommt.
- **Keine Duplikatprüfung per ISBN.** Steht explizit außerhalb des v1-Scopes.
- **Hibernate DDL `create`** erzeugt die Tabelle `catalog_book` automatisch beim nächsten Start – konsistent mit allen anderen Tabellen im Projekt.

## So kann ich es testen

### 1. Anwendung starten

```bash
docker-compose up -d
mvn spring-boot:run
```

### 2. Tabelle prüfen (PostgreSQL)

```bash
docker exec -it $(docker ps -qf "name=postgres") psql -U postgres -d loans -c "\dt catalog_book"
```

Erwartete Ausgabe:

```
         List of relations
 Schema |     Name     | Type  |  Owner
--------+--------------+-------+----------
 public | catalog_book | table | postgres
```

### 3. Spalten prüfen

```bash
docker exec -it $(docker ps -qf "name=postgres") psql -U postgres -d loans -c "\d catalog_book"
```

Erwartete Spalten: `id`, `isbn`, `title`, `author`, `cover_url`, `open_library_key`, `added_at`.

### 4. Kompilierung

```bash
mvn compile
```

Keine Fehler, keine Warnungen zu fehlenden Abhängigkeiten.

### 5. Isolation prüfen

```bash
grep -r "import de.demo.lending.catalog" src/main/java/de/demo/lending/loan
grep -r "import de.demo.lending.catalog" src/main/java/de/demo/lending/inventory
grep -r "import de.demo.lending.catalog" src/main/java/de/demo/lending/procurement
grep -r "import de.demo.lending.catalog" src/main/java/de/demo/lending/payment
```

Alle vier Befehle sollten keine Ausgabe liefern.
