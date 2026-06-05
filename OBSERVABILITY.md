# Lokaler Observability Stack

Zentralisiertes Log-Management für das Lending System mit **Loki**, **Promtail** und **Grafana** – lokal via Docker Compose, ohne Cloud-Abhängigkeiten.

---

## Schnellstart

```bash
# Infrastruktur + Observability-Stack starten
docker compose -f docker-compose.observability.yml up -d

# Spring Boot App starten (separates Terminal)
mvn spring-boot:run
```

**Grafana aufrufen:** http://localhost:3000

| | |
|---|---|
| **Benutzername** | `admin` |
| **Passwort** | `admin` |

---

## Beteiligte Komponenten

| Komponente | Rolle | Port | Image |
|------------|-------|------|-------|
| **Loki** | Log-Aggregation – nimmt Logs entgegen und indexiert Labels | 3100 | `grafana/loki:3.0.0` |
| **Promtail** | Log-Shipper – liest Container-Logs via Docker-Socket und App-Logs aus Datei | – | `grafana/promtail:3.0.0` |
| **Grafana** | Dashboard & Visualisierung – Loki als vorkonfigurierte Datenquelle | 3000 | `grafana/grafana:11.0.0` |
| **PostgreSQL** | Anwendungsdatenbank | 5432 | `postgres:15-alpine` |
| **Kafka** | Event Streaming | 9092 | `confluentinc/cp-kafka:latest` |

### Log-Quellen

| Quelle | Wie erfasst |
|--------|-------------|
| PostgreSQL-Container | Promtail via Docker-Socket |
| Kafka-Container | Promtail via Docker-Socket |
| Spring Boot App (lokal) | Promtail liest `logs/lending-system.log` |

---

## Grafana: Logs finden

### 1. Vorkonfiguriertes Dashboard öffnen

**Dashboards → Lending System → Lending System – Logs**

Das Dashboard zeigt:
- Alle Anwendungslogs (letzten 60 Minuten)
- Fehler und Warnungen gefiltert

### 2. Explore-Modus (Ad-hoc Queries)

**Explore → Loki auswählen → LogQL eingeben**

---

## LogQL-Beispielabfragen

### Alle Logs des Lending-Systems anzeigen

```logql
{compose_project="lending-system"}
```

### Logs eines bestimmten Services filtern

```logql
{service="postgres"}
```

```logql
{container="lending-kafka"}
```

### Spring Boot App-Logs (lokaler Start)

```logql
{service="lending-app"}
```

### Nur Fehler anzeigen

```logql
{compose_project="lending-system"} |= "ERROR"
```

### Nur WARN und ERROR

```logql
{compose_project="lending-system"} | logfmt | level =~ "ERROR|WARN"
```

### Kafka-Events tracken

```logql
{compose_project="lending-system"} |= "Publishing event"
```

### Ausleihe-Flow verfolgen (Correlation-ID)

```logql
{compose_project="lending-system"} |= "correlationId"
```

### Log-Volumen pro Service (Metrik aus Logs)

```logql
sum by (service) (
  count_over_time({compose_project="lending-system"}[5m])
)
```

---

## Stack stoppen

```bash
# Stack stoppen (Daten bleiben erhalten)
docker compose -f docker-compose.observability.yml down

# Stack stoppen und alle Daten löschen
docker compose -f docker-compose.observability.yml down -v
```

---

## Konfigurationsdateien

```
infra/observability/
├── loki-config.yml                          # Loki: Storage, Schema, Retention
├── promtail-config.yml                      # Promtail: Log-Quellen, Pipeline-Stages, Labels
└── grafana/
    ├── provisioning/
    │   ├── datasources/
    │   │   └── datasources.yml              # Loki als automatische Datenquelle
    │   └── dashboards/
    │       └── dashboards.yml               # Dashboard-Provider-Konfiguration
    └── dashboards/
        └── lending-logs.json                # Vorkonfiguriertes Logs-Dashboard
```

---

## Hinweise zur lokalen Nutzung

**Spring Boot Logs:** Die App schreibt Logs in `logs/lending-system.log` (konfiguriert in `application.yml`). Promtail liest diese Datei. Logs sind nach dem ersten `mvn spring-boot:run`-Start in Grafana sichtbar.

**Daten-Persistenz:** Loki- und Grafana-Daten werden in Docker-Volumes gespeichert (`loki-data`, `grafana-data`). Bei `down -v` gehen alle Logs verloren.

**macOS-Hinweis:** Promtail nutzt den Docker-Socket (`/var/run/docker.sock`), der von Docker Desktop auf macOS bereitgestellt wird. Keine zusätzliche Konfiguration nötig.

**Abgrenzung zu Produktion:** Dieser Stack ist für lokale Entwicklung und Demo-Zwecke ausgelegt. Für Kubernetes-Produktion siehe `docs/observability.md`.

---

## Schnelldiagnose

```bash
# Loki-Status prüfen
curl http://localhost:3100/ready

# Loki-Labels abfragen
curl http://localhost:3100/loki/api/v1/labels

# Promtail-Status
curl http://localhost:9080/ready

# Grafana Health
curl http://localhost:3000/api/health
```
