# ADR-003: Anti-Corruption Layer für die Open Library API Integration

## Status

Accepted

## Kontext

Der Catalog-Kontext benötigt Buchdaten aus externen Quellen. Die **OpenLibrary API** (openlibrary.org) stellt einen öffentlichen, kostenlosen Buchkatalog bereit und ist die naheliegendste Datenquelle für das System.

Die OpenLibrary API verwendet ein eigenes Datenmodell und eine eigene Terminologie: Bücher werden als „Works" (`/works/OL...W`) und „Editions" modelliert, Autoren als separate Ressourcen mit eigenen Schlüsseln. Dieses Modell unterscheidet sich fundamental von der internen Domänensprache des Systems, in der ein Buch ein `CatalogBook` mit ISBN, Titel, Autor und Metadaten ist.

Ohne explizite Isolierung würde die externe API-Terminologie in das interne Domänenmodell eindringen. Änderungen an der OpenLibrary-API (neue Felder, geänderte Schlüsselstrukturen, API-Versionierung) würden direkt die interne Domäne berühren. Dies ist ein klassisches Szenario für den Einsatz eines **Anti-Corruption Layer (ACL)** nach Eric Evans.

Darüber hinaus ist die OpenLibrary API eine externe Abhängigkeit, deren Verfügbarkeit und Konsistenz nicht kontrollierbar ist. Die Architektur muss diese Unsicherheit kapseln.

## Entscheidung

Die Integration der OpenLibrary API erfolgt über einen dedizierten **Anti-Corruption Layer**, der als Outbound-Adapter innerhalb der hexagonalen Architektur des Catalog-Kontexts implementiert wird.

Der ACL besteht aus zwei Schichten:

1. **Output Port (Interface):** `LibrarySearchClient` im Application-Layer des Catalog-Kontexts definiert den Vertrag aus Sicht der Domäne. Rückgabe ist `List<BookSearchResult>` – ein internes Transferobjekt ohne Bezug zur OpenLibrary-Terminologie.

2. **Adapter (Implementierung):** `OpenLibraryCatalogAdapter` übersetzt zwischen der internen Domänensprache und dem OpenLibrary-API-Modell. Die Übersetzungslogik (Feldmapping, Fallback-Strategien, Cover-URL-Konstruktion) ist vollständig in diesem Adapter gekapselt.

Dieselbe Kapselungsstrategie gilt für den Procurement-Kontext: `OpenLibraryClientAdapter` implementiert den `ProcurementClient`-Port und isoliert die ISBN-basierte Buchsuche für den Beschaffungsfluss.

Die externe Abhängigkeit ist hinter dem Interface austauschbar: Eine andere Bibliotheks-API (z. B. Google Books, Deutsche Nationalbibliothek, Worldcat) kann durch eine neue Adapter-Implementierung eingebunden werden, ohne die Domäne oder den Application Service zu berühren.

## Konsequenzen

### Vorteile

- **Stabilität der Domänensprache:** Das interne Domänenmodell (`CatalogBook`, `BookSearchResult`) ist vollständig entkoppelt von der externen API-Struktur. Änderungen an der OpenLibrary-API sind auf den Adapter beschränkt.
- **Austauschbarkeit:** Der externe Datenanbieter kann gewechselt werden (z. B. bei Kostenanpassungen oder besserer Datenqualität), ohne Domäne oder Application Service zu ändern.
- **Testbarkeit:** Der Output-Port `LibrarySearchClient` kann in Tests durch eine Test-Implementierung oder einen Mock ersetzt werden. Die Domänenlogik ist ohne Netzwerkzugriff testbar.
- **Fehlerkapselung:** Netzwerkfehler, HTTP-Fehlercodes und unvollständige Antworten der externen API werden im Adapter behandelt. Nach außen sieht der Application Service immer eine definierte Rückgabe (leere Liste statt Exception).
- **Explizite Semantik:** Die Übersetzung vom OpenLibrary-Modell (Works, Editions) in das interne Modell (CatalogBook) wird explizit sichtbar und wartbar.

### Nachteile

- **Übersetzungsaufwand:** Jede Erweiterung des internen Modells (neue Felder) erfordert eine Anpassung der Übersetzungslogik im Adapter.
- **Datenverlust durch Mapping:** Informationen, die in der externen API vorhanden sind, aber nicht in das interne Modell übernommen werden, gehen verloren. Die Auswahl der relevanten Felder ist eine bewusste Designentscheidung.
- **Keine Echtzeit-Synchronisation:** Das System cached keine OpenLibrary-Daten. Bei jedem Suchaufruf wird die externe API kontaktiert. Latenz und Verfügbarkeit der API beeinflussen die Antwortzeiten direkt.
- **Fehlende Timeout-Konfiguration:** In der aktuellen Implementierung ist kein expliziter Timeout für den `RestTemplate`-Aufruf konfiguriert. Dies ist eine bekannte technische Schuld (Backlog-Kandidat).

## Betrachtete Alternativen

**Direkte Integration ohne ACL:** Der Application Service ruft die OpenLibrary-API direkt auf und verarbeitet das JSON-Rohantwortformat. Einfacher initial, aber das externe Modell dringt direkt in die Anwendungslogik ein. Jede API-Änderung erfordert Anpassungen in der Geschäftslogik.

**Shared Kernel mit OpenLibrary-Typen:** Das interne System übernimmt Typen und Terminologie der OpenLibrary-API als Shared Kernel. Reduziert Übersetzungsaufwand, schafft aber eine starke externe Abhängigkeit im Herzen der Domäne. Verstößt gegen das Prinzip der strategischen Isolation in DDD.

**Eigener Datenspiegel (Read Replica):** Ein dedizierter Service importiert periodisch OpenLibrary-Daten in eine eigene Datenbank. Bessere Verfügbarkeit und Performance, aber erheblicher Betriebsaufwand, Datenlizenzfragen und Aktualitätsprobleme.

**GraphQL-Aggregation-Layer:** Ein Gateway-Service aggregiert mehrere externe Quellen. Sinnvoll bei vielen externen Datenquellen, überengineered für einen einzelnen Provider.

## Referenzen

- Evans, Eric: *Domain-Driven Design*, Addison-Wesley, 2003, Kapitel: Anti-Corruption Layer
- Fowler, Martin: *Anti Corruption Layer*, martinfowler.com/eaaCatalog
- Hohpe, Gregor & Woolf, Bobby: *Enterprise Integration Patterns*, Addison-Wesley, 2003
- OpenLibrary API Documentation: openlibrary.org/developers/api
- iSAQB Curriculum: Modul EAI (Integrationsarchitektur), Pattern: Translator/Mapper
