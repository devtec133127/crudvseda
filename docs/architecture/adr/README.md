# Architecture Decision Records

Architekturentscheidungen für das **Library Lending System** – ein Referenzprojekt für  
Domain-Driven Design, Event-Driven Architecture und Cloud-native Betrieb.

Alle ADRs folgen dem Template von **Michael Nygard** und sind nach iSAQB CPSA-A-Standards verfasst.

---

## Übersicht

| ADR | Titel | Status | Bereich |
|-----|-------|--------|---------|
| [ADR-001](./adr-001-domain-driven-design.md) | Domain-Driven Design mit Bounded Contexts | Accepted | Architekturstil |
| [ADR-002](./adr-002-event-driven-architecture.md) | Event-Driven Architecture mit Apache Kafka | Accepted | Integrationsarchitektur |
| [ADR-003](./adr-003-open-library-anti-corruption-layer.md) | Anti-Corruption Layer für Open Library Integration | Accepted | Integrationsarchitektur |
| [ADR-004](./adr-004-ai-enrichment-service.md) | AI Enrichment Service als eigenständiger Microservice | Accepted | Systemdekomposition |
| [ADR-005](./adr-005-observability-loki-grafana.md) | Observability mit Loki, Fluent-Bit und Grafana | Accepted | Betriebsmodell |
| [ADR-006](./adr-006-kubernetes-deployment.md) | Kubernetes als Zielbetriebsmodell | Accepted | Betriebsmodell |
| [ADR-007](./adr-007-deployment-monolith.md) | Einzelnes Deployment-Artefakt statt Microservice pro Bounded Context | Accepted | Deployment-Architektur |

---

## Abhängigkeiten zwischen den ADRs

```
ADR-001 (DDD)
    └─ ADR-002 (EDA)          ADR-001 ist Voraussetzung: Bounded Contexts kommunizieren via Events
    │    └─ ADR-003 (ACL)     Externe Systeme werden über Events in den internen Kontext integriert
    │    └─ ADR-004 (AI)      AI Service ist ein weiterer Event-Consumer im EDA-Ökosystem
    └─ ADR-007 (Modulith)     Bounded Contexts sind Designgrenzen, keine Deployment-Einheiten
         └─ ADR-004 (AI)      Erste bewusste Extraktion aus dem Modulith (Ausnahme, nicht Regel)
         └─ ADR-006 (K8s)     Zielplattform für das einzelne Deployment-Artefakt
              └─ ADR-005 (Loki) Observability ist Voraussetzung für produktiven K8s-Betrieb
```

---

## Vorlage für neue ADRs

```markdown
# ADR-XXX: Titel

## Status
Proposed | Accepted | Deprecated | Superseded by ADR-YYY

## Kontext
[Welches Problem oder welche Anforderung führt zu dieser Entscheidung?]

## Entscheidung
[Was wurde entschieden?]

## Konsequenzen

### Vorteile
- ...

### Nachteile
- ...

## Betrachtete Alternativen
[Welche Optionen wurden verworfen und warum?]

## Referenzen
- ...
```
