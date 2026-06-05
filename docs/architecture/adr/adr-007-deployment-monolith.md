# ADR-007: Einzelnes Deployment-Artefakt statt Microservice pro Bounded Context

## Status

Accepted

## Kontext

Das System ist nach Domain-Driven Design in fünf Bounded Contexts strukturiert (`loan`, `inventory`, `procurement`, `payment`, `catalog`). Eine häufige, aber sachlich unzutreffende Schlussfolgerung ist, dass aus jedem Bounded Context zwingend ein eigenständiger Microservice folgen müsse. Diese Gleichsetzung von Designgrenzen und Deployment-Einheiten ist ein verbreitetes Missverständnis in der DDD-Praxis.

Das Projekt steht vor der Entscheidung: Sollen die Bounded Contexts als separate deploybare Einheiten (5 Spring Boot Applikationen, 5 Container, 5 Deployment-Pipelines) oder als ein einziges kohärentes Deployment-Artefakt realisiert werden?

Folgende Faktoren prägen den Entscheidungskontext:

- Das System ist ein **Referenzprojekt**, dessen Kernziel die Demonstration von DDD- und EDA-Architekturmustern ist – nicht die Lösung operativer Produktionsprobleme.
- Die Bounded-Context-Grenzen sind im Code durch strikte Paketstruktur und ArchUnit-Regeln durchgesetzt, nicht durch Netzwerkgrenzen.
- Die interne Kommunikation zwischen Kontexten erfolgt bereits über Domain Events – eine Grundvoraussetzung, die eine spätere Extraktion in eigenständige Services ermöglicht.
- Das Team (ein Entwickler) würde durch fünf separate Services und deren operativen Overhead signifikant belastet.

## Entscheidung

Alle Bounded Contexts werden in **einem einzigen Spring Boot Deployment-Artefakt** (JAR-Datei) gebündelt und als ein Prozess betrieben.

Die Architektur folgt dem **Modulith-Pattern**: Die fachlichen Grenzen existieren als strikte Modulstrukturen zur Entwicklungszeit, ohne dass diese Grenzen zur Laufzeit durch Netzwerkgrenzen erzwungen werden.

Kernprinzipien der Entscheidung:

- **Bounded Context ≠ Microservice:** Ein Bounded Context ist ein Designkonzept (Domänengrenze, Ubiquitous Language, Modellgrenze), keine Deployment-Einheit. Diese Trennung ist explizit.
- **Modulare Codestruktur als primäre Grenze:** Die Isolation der Kontexte wird durch Paketkonventionen (`de.demo.lending.loan`, `de.demo.lending.catalog`, …), keine direkten Cross-Context-Imports und optionale ArchUnit-Regeln durchgesetzt.
- **Event-basierte Kommunikation trotz Same-JVM:** Die Bounded Contexts kommunizieren ausschließlich über Domain Events (via `@Async` oder Kafka), auch innerhalb desselben Prozesses. Dies simuliert die Kommunikationssemantik verteilter Services und bereitet die spätere Extraktion vor.
- **Entkopplung auf Designebene, nicht auf Deployment-Ebene:** Die Hexagonale Architektur stellt sicher, dass jeder Bounded Context technologisch isoliert und unabhängig testbar ist.
- **Schrittweise Extraktion (Strangler Fig):** AI Enrichment Service (ADR-004) ist das erste Beispiel einer bewussten Extraktion in einen eigenständigen Service – wenn ein fachlicher oder technischer Grund dafür vorliegt, nicht als Standardmuster für jeden Context.

Das Deployment-Artefakt ist ein einziges Docker-Image, das aus dem `Dockerfile` im Projektwurzel gebaut wird.

## Konsequenzen

### Vorteile

- **Entwicklungsgeschwindigkeit:** Ein einziger Startprozess (`mvn spring-boot:run`), kein Service-Mesh, keine Docker-Netzwerkkonfiguration zwischen Applikationskomponenten. Feedback-Zyklen bleiben kurz.
- **Demonstrationsklarheit:** Architekturmuster (DDD, EDA, Hexagonale Architektur) sind ohne operativen Overhead sichtbar. Beobachter müssen kein Kubernetes-Cluster oder Service-Registry verstehen, um die Kernaussagen nachzuvollziehen.
- **Operational Simplicity:** Ein Deployment-Prozess, eine Konfigurationsdatei, ein Healthcheck, ein Log-Stream. Die operative Komplexität wächst nicht mit der Anzahl der Bounded Contexts.
- **Atomare Transaktionen innerhalb eines Kontexts:** Da alle Contexts in einer JVM laufen, sind transaktionale Garantien innerhalb eines Kontexts vollständig erfüllbar, ohne verteilte Transaktionen oder Saga-Muster erzwingen zu müssen.
- **Explizite Migrierbarkeit:** Die strikte Paketstruktur und die Event-basierte Kommunikation zwischen Contexts machen jeden Bounded Context zu einem Kandidaten für eine spätere Extraktion. Der Aufwand einer solchen Migration ist durch die saubere Architektur minimal – das ist der Wert des Modulith-Ansatzes.
- **Referenzarchitektur-Qualität:** Die Muster (DDD-Aggregate, Events, Ports & Adapters) sind klar sichtbar, weil sie nicht hinter Kubernetes-YAML und Service-Registrierungen verborgen sind.

### Nachteile

- **Shared Failure Domain:** Ein Fehler in einem Bounded Context (z. B. Memory Leak im Payment-Context) kann alle anderen Contexts im selben Prozess beeinflussen. In einer Microservices-Architektur wäre die Fehlerdomäne auf den betroffenen Service begrenzt.
- **Skalierung als Einheit:** Der gesamte Monolith muss horizontal skaliert werden, auch wenn nur ein einzelner Bounded Context unter Last steht. Feingranulare Skalierung ist nicht möglich.
- **Technologische Homogenität erzwungen:** Alle Bounded Contexts müssen dieselbe Technologie (Java, Spring Boot) verwenden. In einem Microservices-Setup könnte ein Context in Python, ein anderer in Go implementiert sein.
- **Deployment-Kopplung:** Eine Änderung in einem Bounded Context erfordert den Neustart des gesamten Artefakts. Zero-Downtime-Deployments für einzelne Contexts sind nicht möglich.

## Betrachtete Alternativen

**Microservice pro Bounded Context (5 separate Deployments):** Hätte die volle operative Unabhängigkeit jedes Contexts ermöglicht. Der operative Overhead (5 Deployment-Pipelines, Service Discovery, Inter-Service-Authentifizierung, Distributed Tracing über 5 Services, 5 Datenbankverbindungen) wäre für ein Referenzprojekt jedoch prohibitiv und würde die eigentlichen Architekturaussagen hinter operativer Komplexität verbergen. Zusätzlich besteht bei voreiligen Microservice-Schneidungen das Risiko, Bounded Context-Grenzen falsch zu ziehen – ein Fehler, der im Monolithen günstiger refaktorierbar ist.

**Spring Modulith:** Eine explizite Implementierung des Modulith-Patterns mit dem Spring Modulith Framework (Modulgrenzen als Spring-Beans-Barrieren, automatische Modultests). Würde die internen Grenzen technisch durchsetzen, ist aber für das aktuelle Projektstadium ein zusätzlicher Framework-Layer ohne signifikanten Mehrwert gegenüber der bestehenden Paketstruktur. Als nächste Evolution des Projekts sinnvoll.

**Domain-spezifische Service-Extraktion:** Statt alle Contexts zu extrahieren, würden nur Contexts mit spezifischen Anforderungen (unterschiedliche Skalierungsanforderungen, andere Technologie) als eigenständige Services betrieben. Dies ist die implizite Strategie des Projekts: Der AI Enrichment Service (ADR-004) ist das erste Beispiel einer solchen zielgerichteten Extraktion. Der Catalog Service könnte folgen, wenn Skalierungsanforderungen es erfordern.

## Referenzen

- Newman, Sam: *Monolith to Microservices*, O'Reilly, 2019 (Kapitel: Should I decompose my monolith?)
- Fowler, Martin: *Monolith First*, martinfowler.com/bliki/MonolithFirst.html
- Fowler, Martin: *Strangler Fig Application*, martinfowler.com/bliki/StranglerFigApplication.html
- Richardson, Chris: *Pattern: Monolithic Architecture*, microservices.io/patterns/monolithic.html
- Vernon, Vaughn: *Implementing Domain-Driven Design*, Addison-Wesley, 2013, Kapitel 2: Domains, Subdomains, and Bounded Contexts
- Spring Modulith: spring.io/projects/spring-modulith
- iSAQB Curriculum: Modul IMPROVE (Evolvierbarkeit von Architekturen)
