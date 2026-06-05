# Observability – Lokaler Monitoring-Stack

## Ziel

Diese Erweiterung integriert einen lokalen Observability-Stack in das Referenzprojekt, ohne Cloud-Abhängigkeiten oder kostenpflichtige Dienste. Ziel ist die Demonstration professioneller Betriebskonzepte im Rahmen eines lokalen Entwicklungssetups – präsentierbar, nachvollziehbar und als Einstiegspunkt für eine spätere Cloud- oder Kubernetes-Variante geeignet.

---

## Unterstützte Qualitätsmerkmale (ISO 25010)

| Qualitätsmerkmal | Wie adressiert |
|------------------|----------------|
| **Betreibbarkeit** | Zentrale Logs aller Container in Grafana sichtbar; keine manuelle Log-Suche in Terminals |
| **Analysierbarkeit** | Strukturierte Log-Labels (Service, Container, Log-Level) ermöglichen gezieltes Filtern mit LogQL |
| **Wartbarkeit** | Konfiguration als Code (YAML); reproduzierbarer Stack via `docker compose up` |
| **Transparenz** | Alle Architekturentscheidungen in ADR-005 dokumentiert |

---

## Architekturentscheidung

Gemäß **ADR-005** wurde der **Grafana Observability Stack** (Loki + Promtail + Grafana) gewählt. Die Entscheidung basiert auf:

- **Open-Source, keine Lizenzkosten** – vollständig kostenfrei für lokale und produktive Nutzung
- **Cloud-Agnostizität** – kein Lock-in zu einem Cloud-Provider
- **Kubernetes-nativer Ansatz** – dieselben Tools werden in der Zielarchitektur (K8s) eingesetzt; die lokale Konfiguration ist ein direkter Vorläufer der Produktionskonfiguration
- **Loki's Designphilosophie** – indexlos, schemafrrei, kosteneffizient; Logs werden wie Metrikdaten behandelt (Labels statt Volltextindex)

---

## Warum lokale Docker-basierte Observability ausreichend ist

Dieses Projekt ist ein **Referenzprojekt zur Architekturdemonstration**, kein produktives System. Die lokale Observability-Lösung erfüllt folgende Anforderungen vollständig:

1. Demonstriert das Observability-Prinzip (Logs zentralisieren, filtern, visualisieren)
2. Ist ohne Cloud-Account, VPN oder externe Dienste nutzbar
3. Zeigt die Konfigurationsmuster, die 1:1 in Kubernetes übertragbar sind
4. Reduziert die kognitive Last: Entwickler sehen Container-Logs ohne Terminal-Debugging

---

## Grenzen dieser Lösung

| Grenze | Beschreibung |
|--------|-------------|
| **Kein Distributed Tracing** | OpenTelemetry + Grafana Tempo sind nicht integriert; Correlation-IDs im Log-Format sind der Kompromiss |
| **Kein Alerting** | Grafana Alertmanager ist nicht konfiguriert; für Produktionsbetrieb notwendig |
| **Keine Metriken** | Prometheus ist nicht integriert (erfordert `micrometer-registry-prometheus` Dependency); Loki-Metriken via LogQL sind verfügbar |
| **Kein Persistenz-Konzept** | Loki-Daten liegen in einem Docker-Volume; bei `docker compose down -v` gehen Logs verloren |
| **Lokale Laufzeit** | Promtail liest Container-Logs via Docker-Socket; in Kubernetes wird stattdessen ein DaemonSet mit Fluent-Bit eingesetzt |

---

## Spätere Cloud- / Kubernetes-Variante

In der Zielarchitektur (Kubernetes, gemäß ADR-006) werden dieselben Werkzeuge verwendet, aber mit cloud-nativen Deployment-Patterns:

```
Lokale Variante (heute)          →   Kubernetes-Variante (Ziel)
──────────────────────────────────────────────────────────────────
Promtail im Docker-Container     →   Promtail / Fluent-Bit als DaemonSet
Loki im Docker-Volume            →   Loki mit S3 Object Storage (Chunks)
                                     oder Grafana Cloud Loki
Grafana im Docker-Container      →   Grafana Operator / Helm Chart
Manuelle Compose-Konfiguration   →   Helm Values + GitOps (ArgoCD/Flux)
Docker-Socket Mounting           →   Kubernetes Pod-Log-API
```

**Migrationsstrategie:** Die Loki-Konfiguration und Promtail-Pipeline-Stages aus diesem Setup sind direkt in Kubernetes-Konfigurationen übertragbar. Nur das Deployment-Modell ändert sich, nicht die Observability-Logik.
