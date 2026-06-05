# ADR-002: Einsatz einer Event-Driven Architecture mit Apache Kafka als Message Broker

## Status

Accepted

## Kontext

Die in ADR-001 definierten Bounded Contexts müssen miteinander kommunizieren, ohne ihre fachliche Autonomie aufzugeben. Eine Ausleihe (`loan`) führt beispielsweise zur Reservierung eines Exemplars (`inventory`), zur optionalen Beschaffung (`procurement`) und zur Initiierung einer Zahlung (`payment`). Diese Abläufe sind fachlich zusammengehörig, aber technisch entkoppelt.

Eine direkte synchrone Kopplung der Kontexte über REST-Aufrufe würde die Bounded-Context-Isolation unterlaufen: Der Loan-Service müsste die internen APIs des Inventory-Service, des Payment-Service und des Procurement-Service kennen. Dies erzeugt eine Laufzeit- und Designkoppllung, die bei Änderungen kaskadiert.

Darüber hinaus stellt sich die Frage der Orchestrierung versus Choreographie: Soll ein zentraler Koordinator (Orchestrator) den Ablauf steuern, oder sollen die Kontexte eigenständig auf Ereignisse reagieren?

## Entscheidung

Das System folgt einem **choreographiebasierten Event-Driven Architecture (EDA)**-Muster. Domain Events sind das primäre Kommunikationsmittel zwischen Bounded Contexts. Kein Kontext kennt die Implementierung eines anderen; jeder Kontext reagiert ausschließlich auf Events, die er versteht.

**Apache Kafka** wird als Message Broker eingesetzt (KRaft-Modus, kein ZooKeeper). Kafka bietet persistente, geordnete Event-Logs, die als Single Source of Truth für den Zustand eines Geschäftsprozesses dienen können.

Kernprinzipien der Entscheidung:

- **Domain Events** werden innerhalb eines Bounded Context erzeugt und veröffentlicht (z. B. `LoanRequested`, `BookReserved`, `ProcurementInitiated`)
- **Event-Topics** folgen einem versionierten Namensschema (`loan.requested.v1`)
- **Idempotente Verarbeitung** wird durch eine Inbox-Tabelle (`processed_event`) und die idempotence4j-Bibliothek sichergestellt
- Das **Outbox-Pattern** ist architektonisch vorbereitet (Tabelle `outbox`), um atomares Schreiben und Event-Publikation zu gewährleisten
- Für lokale Entwicklung ohne Kafka-Infrastruktur steht ein Spring-`@Async`-Profil zur Verfügung, das denselben Ablauf in-memory simuliert

## Konsequenzen

### Vorteile

- **Zeitliche und räumliche Entkopplung:** Produzent und Konsument müssen nicht gleichzeitig verfügbar sein. Ein Service kann Events verarbeiten, wenn er wieder online ist (Resilience).
- **Unabhängige Skalierung:** Jeder Kontext skaliert nach seinen eigenen Lastprofilen, ohne den Produzenten zu beeinflussen.
- **Audit-Trail:** Das Kafka-Log ist ein unveränderlicher, zeitgeordneter Ereignisstrom, der Compliance- und Debugging-Anforderungen erfüllt.
- **Erweiterbarkeit:** Neue Konsumenten (z. B. AI Enrichment Service) können Events abonnieren, ohne dass der Produzent angepasst wird. Dieses Open/Closed-Prinzip auf Systemebene ist ein zentrales Qualitätsmerkmal.
- **Event Sourcing als Option:** Die Event-orientierte Grundstruktur ermöglicht später den Einstieg in Event Sourcing, falls Auditierbarkeit und Zeitreisen im Zustand gefordert werden.

### Nachteile

- **Eventual Consistency:** Der Systemzustand ist zu einem gegebenen Zeitpunkt möglicherweise inkonsistent. Für Domänen, die starke Konsistenz erfordern (z. B. Zahlungsabwicklung), sind zusätzliche Maßnahmen notwendig.
- **Erhöhte operative Komplexität:** Kafka erfordert Monitoring, Retention-Konfiguration, Consumer-Group-Management und Disaster-Recovery-Konzepte.
- **Debugging und Tracing:** Verteilte Abläufe über mehrere Events sind schwerer nachzuvollziehen als synchrone Aufrufketten. Distributed Tracing (Correlation-IDs, OpenTelemetry) ist verpflichtend.
- **Idempotenz:** Jeder Konsument muss mit doppelter Event-Lieferung umgehen können (At-Least-Once-Semantik). Dies erfordert explizite Implementierung in jedem Kontext.
- **Lokale Entwicklung:** Die Kafka-Infrastruktur muss lokal verfügbar sein (Docker Compose). Das `@Async`-Profil federt dies ab, schafft aber eine Abweichung vom Produktionsverhalten.

## Betrachtete Alternativen

**Synchrone REST-Kommunikation (Request/Response):** Einfacher zu implementieren und zu debuggen, führt aber zu direkter Laufzeitkopplung. Ein Ausfall des Inventory-Service würde den Loan-Service blockieren. Nicht geeignet für Systeme mit Resilience-Anforderungen.

**Orchestriertes Saga-Pattern (z. B. mit Temporal oder Camunda):** Ein zentraler Orchestrator koordiniert den Ablauf und kennt jeden Schritt explizit. Bietet bessere Sichtbarkeit des Gesamtprozesses, schafft aber einen Single Point of Control und -Failure. Geeignet für lange, kompensierbare Transaktionen.

**In-Memory Event Bus (Spring ApplicationEvent):** Funktional für einen Monolithen, aber ohne Persistenz, Replay-Fähigkeit und verteilte Verarbeitung. Nur als Entwicklungsprofil (`async`) im Projekt enthalten.

**RabbitMQ als Message Broker:** Gute Wahl für klassische Request/Reply- und Work-Queue-Szenarien. Kafka ist überlegen für Event-Log-Semantik, hohen Durchsatz und Consumer-Group-Isolation.

## Referenzen

- Richardson, Chris: *Microservices Patterns*, Manning, 2018 (Kapitel: Saga, Transactional Outbox)
- Fowler, Martin: *What do you mean by Event-Driven?*, martinfowler.com, 2017
- Kleppmann, Martin: *Designing Data-Intensive Applications*, O'Reilly, 2017
- iSAQB Curriculum: Modul EAI (Enterprise Application Integration), Modul CLOUD
- Apache Kafka Documentation: KRaft Mode, Consumer Groups, Idempotent Producers
