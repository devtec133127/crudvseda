# ADR-004: AI Enrichment Service als eigenständiger Microservice

## Status

Accepted

## Kontext

Die Anreicherung von Buchmetadaten durch KI-Verfahren ist als Erweiterung des Systems geplant. Ziel ist es, zu einem `CatalogBook`-Eintrag automatisch Zusammenfassungen, Themenkategorien, Schwierigkeitsgrade und ähnliche Bücher zu generieren – Daten, die in externen Quellen (OpenLibrary, Google Books) nicht oder nur unvollständig vorhanden sind.

Die Frage der Systemarchitektur stellt sich konkret: Soll diese Anreicherungslogik **innerhalb des bestehenden Catalog Service** implementiert werden, oder als **eigenständiger Service**?

Folgende Faktoren prägen den Entscheidungskontext:

- KI-Infrastruktur (LLM-APIs, GPU-Beschleunigung, Prompt-Engineering) hat fundamental andere nichtfunktionale Anforderungen als ein CRUD-orientierter Catalog Service.
- LLM-Technologien (OpenAI, Anthropic Claude, lokale Modelle mit Ollama) entwickeln sich rapid. Die Wahl des Providers und Modells muss ohne Auswirkungen auf den Katalog geändert werden können.
- Anreicherung ist kein synchroner Vorgang: Ein LLM-Aufruf kann mehrere Sekunden dauern. Ein blockierender Aufruf im Catalog Service würde dessen Verfügbarkeit direkt beeinflussen.
- Die KI-Komponente ist optional: Der Catalog Service muss vollständig funktionieren, auch wenn der Enrichment Service nicht verfügbar ist.

## Entscheidung

Der AI Enrichment Service wird als **eigenständiger Microservice** außerhalb des Catalog Service implementiert und betrieben.

Die Integration erfolgt über die bestehende **Event-Driven Architecture** (siehe ADR-002):

1. Der Catalog Service publiziert ein `catalog.book_added.v1`-Event, wenn ein neues Buch in den Katalog aufgenommen wird.
2. Der AI Enrichment Service abonniert dieses Event über den Message Broker (Apache Kafka).
3. Der Enrichment Service ruft das LLM auf, generiert Metadatenvorschläge und publiziert ein `catalog.book_enriched.v1`-Event.
4. Der Catalog Service konsumiert dieses Event und aktualisiert den entsprechenden Katalogeintrag.

Der AI Enrichment Service ist damit ein reiner **Event Consumer und Producer** – er kennt keine internen APIs des Catalog Service und teilt keine Datenbank.

Die technologische Implementierung des Enrichment Service ist bewusst nicht festgelegt: Python mit LangChain, Java mit Spring AI oder Node.js mit dem Anthropic SDK sind gleichwertig zulässig. Die Entkopplung über Events ermöglicht diese Freiheit.

## Konsequenzen

### Vorteile

- **Unabhängige Skalierung:** LLM-Aufrufe sind ressourcenintensiv und latenzreich. Der Enrichment Service kann auf GPU-optimierten Knoten betrieben und unabhängig vom Catalog Service skaliert werden.
- **Technologische Autonomie:** Die KI-Implementierung kann in der optimalen Technologie (Python, spezialisierte ML-Frameworks) erfolgen, ohne die Java-Codebasis des Catalog Service zu belasten.
- **Resilience durch Entkopplung:** Ein Ausfall des Enrichment Service beeinträchtigt den Catalog Service nicht. Bücher werden ohne KI-Metadaten angelegt und später angereichert, sobald der Service wieder verfügbar ist. Das System degradiert graceful.
- **Providerunabhängigkeit:** Der verwendete LLM-Provider (OpenAI, Anthropic, Google Gemini, lokales Modell) kann gewechselt werden, ohne andere Services zu berühren. Die Entscheidung für einen Provider ist damit reversibel.
- **Open/Closed-Prinzip auf Systemebene:** Der Catalog Service bleibt unverändert. Das Hinzufügen von KI-Anreicherung ist eine additive Erweiterung, keine modifizierende.
- **Klare Verantwortlichkeit:** Der AI Enrichment Service hat eine einzige, klar definierte Verantwortlichkeit (Single Responsibility auf Service-Ebene).

### Nachteile

- **Eventual Consistency der Metadaten:** Frisch importierte Bücher sind zunächst ohne KI-Metadaten. Die Anreicherung erfolgt asynchron mit unbekannter Latenz, abhängig von LLM-API-Verfügbarkeit und -Auslastung.
- **Verteilte Fehlerbehandlung:** Fehler im Enrichment-Prozess (LLM-API-Fehler, Prompt-Failures, Qualitätsprobleme) müssen über Dead-Letter-Queues und Retry-Strategien behandelt werden. Dies erfordert explizite Betriebskonzepte.
- **Operative Komplexität:** Ein weiterer Service bedeutet eine weitere Deployment-Einheit, weitere Monitoring-Konfiguration, weitere Secrets-Verwaltung (LLM-API-Keys) und weitere Netzwerkpolitik.
- **Datenkonsistenz bei Updates:** Wenn ein Katalogeintrag nachträglich manuell geändert wird, muss entschieden werden, ob eine erneute KI-Anreicherung ausgelöst wird. Diese Logik ist nicht trivial.

## Betrachtete Alternativen

**Integration in den Catalog Service:** Die LLM-Aufrufe werden direkt im Catalog Service synchron oder asynchron ausgeführt. Technisch einfacher, aber: der Catalog Service übernimmt eine fremde Verantwortlichkeit (KI-Verarbeitung), wird technologisch komplexer (Java + ML-Libraries), und Ausfälle des LLM-Providers können den gesamten Catalog Service destabilisieren. Verletzt das Single-Responsibility-Prinzip auf Serviceebene.

**Batch-Verarbeitung (nächtlicher Job):** Ein Cronjob reichert alle Bücher periodisch an. Einfach zu implementieren, aber hohe Latenz (bis zu 24 Stunden bis zur Anreicherung), keine ereignisgesteuerte Verarbeitung und ineffizient bei geringer Änderungsrate.

**Synchroner API-Aufruf im Aufnahme-Flow:** Beim Hinzufügen eines Buches wird der LLM-Aufruf synchron ausgeführt. Der Nutzer wartet auf das Ergebnis. Nicht akzeptabel: LLM-Latenz (2–30 Sekunden), Abhängigkeit von externer API-Verfügbarkeit direkt im kritischen Pfad.

**Serverless Function (z. B. AWS Lambda):** Der Enrichment-Prozess wird als Function-as-a-Service implementiert, ausgelöst durch Kafka-Events über einen Event-Bridge. Reduziert operative Komplexität, erhöht Cloud-Provider-Bindung und macht lokale Entwicklung schwieriger.

## Referenzen

- Newman, Sam: *Building Microservices*, O'Reilly, 2. Auflage, 2021
- Fowler, Martin: *Microservices*, martinfowler.com/articles/microservices.html
- Anthropic: Claude API Documentation, docs.anthropic.com
- iSAQB Curriculum: Modul CLOUD (Cloud-native Architekturmuster), Modul DDD
- Spring AI: spring.io/projects/spring-ai
