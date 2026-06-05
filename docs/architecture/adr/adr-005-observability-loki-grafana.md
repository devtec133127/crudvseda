# ADR-005: Zentrale Observability-Lösung mit Loki, Fluent-Bit und Grafana

## Status

Accepted

## Kontext

Das System besteht aus mehreren eigenständigen Services (Catalog Service, AI Enrichment Service, zukünftige weitere Kontexte) und einer Kafka-basierten Kommunikationsinfrastruktur. In einer solchen verteilten Systemlandschaft ist die Fehlerdiagnose ohne zentralisierte Observability erheblich erschwert: Ein einzelner Geschäftsvorfall – etwa eine Ausleihanfrage – durchläuft mehrere Services und erzeugt Logs, Metriken und Traces in verschiedenen Systemen.

Die Zielplattform Kubernetes (siehe ADR-006) erfordert zudem eine Observability-Lösung, die nativ mit dem Container-Orchestrator zusammenarbeitet: Pods werden dynamisch gestartet und gestoppt, IP-Adressen ändern sich, und klassische host-basierte Log-Aggregation ist nicht anwendbar.

Folgende Observability-Dimensionen müssen abgedeckt werden (nach dem Three-Pillars-Modell):

1. **Logs:** Strukturierte, korrelierbare Anwendungslogs aus allen Services
2. **Metriken:** Systemmetriken (CPU, Memory, JVM) und fachliche Metriken (Anfragevolumen, Fehlerrate)
3. **Traces:** Distributed Tracing für End-to-End-Sichtbarkeit eines Geschäftsvorfalls über Service-Grenzen hinweg

## Entscheidung

Als zentrale Observability-Lösung wird der **Grafana Observability Stack** eingesetzt:

- **Loki** (Grafana Labs) für Log-Aggregation: schemafreie, labelbasierte Indexierung von Logs. Loki speichert nur Metadaten (Labels) in einem Index; Log-Inhalte werden komprimiert im Object Storage (lokal oder S3) abgelegt. Dies macht Loki signifikant kostengünstiger als volltextindexierende Alternativen.

- **Fluent-Bit** als Log-Shipper: leichtgewichtiger Agent mit minimalem Ressourcenverbrauch, der als DaemonSet auf jedem Kubernetes-Knoten läuft. Sammelt Container-Logs, reichert sie mit Kubernetes-Metadaten an (Pod-Name, Namespace, Labels) und leitet sie an Loki weiter.

- **Prometheus** für Metriken: Spring Boot Actuator exponiert Metriken im Prometheus-Format über `/actuator/prometheus`. Prometheus scrapet diese Endpunkte periodisch.

- **Grafana** als zentrales Dashboard und Visualisierungs-Frontend: Unifizierte Oberfläche für Logs (Loki), Metriken (Prometheus) und optional Traces (Tempo). Correlation zwischen Logs und Metriken über gemeinsame Labels (Service-Name, Trace-ID).

**Korrelationsstrategie:** Structured Logging mit einheitlichen Feldern (Trace-ID, Correlation-ID, Service-Name) ermöglicht die Verknüpfung eines Kafka-Events mit den Logs aller beteiligten Services.

**Integration mit dem bestehenden Code:** Spring Boot Actuator ist bereits konfiguriert. Die Erweiterung um Prometheus-Metriken und Structured Logging (Logback mit JSON-Format) erfordert minimalen Konfigurationsaufwand.

## Konsequenzen

### Vorteile

- **Einheitliche Observability-Oberfläche:** Entwickler und Betrieb arbeiten in einem einzigen Dashboard (Grafana) für Logs, Metriken und Alerts – keine Kontextwechsel zwischen verschiedenen Tools.
- **Kosteneffizienz:** Loki, Fluent-Bit, Prometheus und Grafana sind Open-Source-Lösungen ohne Lizenzkosten. Loki ist durch seinen indexlosen Ansatz deutlich günstiger zu betreiben als Elasticsearch-basierte Lösungen.
- **Native Kubernetes-Integration:** Der Stack ist für containerisierte Umgebungen konzipiert. Fluent-Bit als DaemonSet, Prometheus-ServiceMonitors und Grafana-Operator sind etablierte Kubernetes-Patterns.
- **Cloud-Agnostizität:** Der Stack ist nicht an einen Cloud-Provider gebunden. Migration zwischen AWS, Azure und GCP erfordert keine Änderungen an der Observability-Konfiguration.
- **Korrelierbarkeit:** Durch einheitliche Labels und Trace-IDs können Logs, Metriken und Traces eines Geschäftsvorfalls über Service-Grenzen hinweg korreliert werden.

### Nachteile

- **Eigenverantwortlicher Betrieb:** Der gesamte Observability-Stack muss selbst betrieben, aktualisiert und gesichert werden. Für kleinere Teams ist dies ein erheblicher Overhead gegenüber SaaS-Lösungen.
- **Fehlende automatische Anomalie-Erkennung:** Loki und Prometheus bieten regelbasierte Alerts (AlertManager), aber keine ML-basierte Anomalie-Erkennung. Diese muss über Grafana-ML-Plugins oder externe Tools nachgerüstet werden.
- **Lernkurve:** LogQL (Loki-Abfragesprache) und PromQL (Prometheus-Abfragesprache) erfordern initiale Einarbeitung. Die Erstellung aussagekräftiger Dashboards ist zeitintensiv.
- **Kein Out-of-the-Box Distributed Tracing:** Für vollständiges Distributed Tracing muss Grafana Tempo zusätzlich konfiguriert und OpenTelemetry in den Services instrumentiert werden. Dies ist für das aktuelle Inkrement nicht umgesetzt.

## Betrachtete Alternativen

**ELK Stack (Elasticsearch, Logstash, Kibana):** Mächtiger, volltextindexierender Log-Stack mit großer Community. Erheblich ressourcenintensiver und teurer im Betrieb als Loki. Für reine Log-Aggregation in den meisten Szenarien überdimensioniert.

**Datadog / New Relic (SaaS):** Vollständige Observability-as-a-Service mit automatischer Anomalie-Erkennung, APM und ausgezeichneter UX. Ausgeschlossen aufgrund signifikanter Lizenzkosten (volumenbasiert), Datenschutzbedenken (Logs verlassen den eigenen Perimeter) und Cloud-Provider-Bindung.

**AWS CloudWatch / Azure Monitor:** Cloud-native Lösungen mit tiefer Integration in die jeweilige Plattform. Nicht geeignet für ein cloud-agnostisches Referenzprojekt. Erzeugen starke Provider-Bindung (Vendor Lock-in).

**OpenTelemetry + Jaeger:** Fokussiert auf Distributed Tracing. Ergänzend zu Loki/Grafana sinnvoll, aber kein vollständiger Ersatz. OpenTelemetry-Instrumentierung ist als zukünftige Erweiterung vorgesehen.

## Referenzen

- Grafana Labs: Loki Documentation, grafana.com/docs/loki
- Prometheus: Monitoring System & Time Series Database, prometheus.io
- Fluent-Bit: Lightweight Log Processor, fluentbit.io
- Beyer, B. et al.: *Site Reliability Engineering*, O'Reilly, 2016 (Kapitel: Monitoring Distributed Systems)
- iSAQB Curriculum: Modul IMPROVE (Querschnittliche Architekturkonzepte), Modul CLOUD
- Spring Boot Actuator: docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
