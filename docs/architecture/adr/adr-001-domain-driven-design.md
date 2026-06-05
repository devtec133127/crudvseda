# ADR-001: Einsatz von Domain-Driven Design mit getrennten Bounded Contexts

## Status

Accepted

## Kontext

Das Library Lending System adressiert mehrere fachlich distinkte Problemdomänen: Buchkatalogisierung, Ausleihe und Rückgabe, externe Beschaffung, Zahlungsabwicklung sowie zukünftig KI-gestützte Metadatenanreicherung. Diese Domänen unterscheiden sich signifikant in ihrer fachlichen Sprache, ihren Verantwortlichkeiten, Änderungsraten und Datenmodellen.

Ein klassischer Monolith mit einem gemeinsamen Domänenmodell führt erfahrungsgemäß zu semantischen Konflikten: Der Begriff „Buch" bedeutet in der Ausleihe (ein physisches Exemplar mit Zustand) etwas fundamental anderes als im Katalog (ein bibliografischer Datensatz) oder in der Beschaffung (eine Bestellposition). Wird dieser Unterschied nicht explizit modelliert, entsteht ein anämisches Domänenmodell, das fachliche Invarianten durch technische Schichten erzwingt anstatt durch die Domäne selbst.

Als Referenzprojekt für Enterprise-Architekturmuster muss das System darüber hinaus demonstrieren, wie DDD-Prinzipien in einer verteilten Systemlandschaft konsequent angewendet werden können.

## Entscheidung

Das System wird nach den Prinzipien des Domain-Driven Design (DDD) strukturiert. Jede fachliche Domäne wird als eigenständiger **Bounded Context** modelliert, mit:

- einem eigenen, in sich konsistenten Domänenmodell (Aggregate Roots, Entities, Value Objects)
- einer kontextspezifischen Ubiquitous Language, die aus der Zusammenarbeit mit Fachexperten entsteht
- klar definierten Kontextgrenzen, an denen Übersetzungen explizit stattfinden

Die Kommunikation zwischen Bounded Contexts erfolgt ausschließlich über explizite Schnittstellen – entweder durch Domain Events (asynchron) oder durch definierte API-Ports (synchron). Direkter Zugriff auf das Domänenmodell eines anderen Kontexts ist architektonisch ausgeschlossen.

Die Implementierungsstrategie innerhalb jedes Bounded Context folgt der **Hexagonalen Architektur** (Ports & Adapters nach Alistair Cockburn), um die Domäne technologisch frei zu halten und testbar zu gestalten.

Die aktuelle Bounded-Context-Landschaft umfasst:

| Bounded Context | Kernverantwortung |
|-----------------|-------------------|
| `catalog` | Buchkatalog: bibliografische Daten, Metadaten |
| `loan` | Ausleihe: Zustandsmaschine, Fristen, Exemplare |
| `inventory` | Bestand: physische Exemplare, Reservierungen |
| `procurement` | Beschaffung: externe Bestellung, Lieferung |
| `payment` | Zahlungsabwicklung: Gebühren, Transaktionen |

## Konsequenzen

### Vorteile

- **Fachliche Ausdrucksstärke:** Jeder Bounded Context spricht die Sprache seiner Domäne. Fachliche Invarianten und Regeln sind im Domänenmodell verankert, nicht in Service-Schichten.
- **Änderungslokalität:** Änderungen an einer Domäne berühren andere Kontexte nicht. Conway's Law wird bewusst genutzt: Teamgrenzen können an Kontextgrenzen ausgerichtet werden.
- **Testbarkeit:** Die Domänenlogik ist frei von technischen Abhängigkeiten und kann ohne Infrastruktur getestet werden.
- **Erweiterbarkeit:** Neue Bounded Contexts (z. B. AI Enrichment) können hinzugefügt werden, ohne bestehende zu verändern.
- **Explizite Übersetzungen:** Semantische Unterschiede zwischen Kontexten werden durch Anti-Corruption Layers sichtbar gemacht, statt implizit verborgen zu bleiben.

### Nachteile

- **Initialkomplexität:** DDD erfordert intensive Zusammenarbeit mit Fachexperten (Event Storming, Domain Modeling Sessions) und einen höheren Aufwand in der initialen Modellierungsphase.
- **Eventual Consistency:** Da Kontexte keine gemeinsamen Transaktionen teilen, muss Datenkonsistenz über Ereignisse hergestellt werden. Dies erhöht die konzeptionelle Komplexität.
- **Context Mapping Pflege:** Die Beziehungen zwischen Bounded Contexts (Customer/Supplier, Conformist, ACL) müssen dokumentiert und gepflegt werden.
- **Overhead für kleine Teams:** In frühen Projektphasen oder bei kleinen Teams kann der DDD-Overhead die Entwicklungsgeschwindigkeit kurzfristig reduzieren.

## Betrachtete Alternativen

**Anämisches Domänenmodell mit zentralem Service-Layer:** Weit verbreitet, aber führt zur Verschiebung fachlicher Logik in technische Services. Fachliche Invarianten werden durch Code-Konventionen statt durch das Modell selbst durchgesetzt. Schwer refaktorierbar bei wachsender Komplexität.

**Gemeinsames Domänenmodell (Shared Kernel):** Alle Domänen nutzen ein gemeinsames Modell. Reduziert Übersetzungsaufwand, schafft aber eine enge fachliche Kopplung. Jede Änderung im Shared Kernel erfordert Koordination aller Teams. Skaliert organisatorisch nicht.

**Modulith als Kompromiss:** Ein Monolith mit klar getrennten Modulen kann eine pragmatische Zwischenstufe darstellen. Für dieses Referenzprojekt explizit ausgeschlossen, da die Demonstration der Bounded-Context-Isolation ein Kernziel ist.

## Referenzen

- Evans, Eric: *Domain-Driven Design: Tackling Complexity in the Heart of Software*, Addison-Wesley, 2003
- Vernon, Vaughn: *Implementing Domain-Driven Design*, Addison-Wesley, 2013
- Fowler, Martin: *BoundedContext*, martinfowler.com/bliki/BoundedContext.html
- iSAQB Curriculum: Modul DOMAIN (Domänenorientierte Architekturmuster)
