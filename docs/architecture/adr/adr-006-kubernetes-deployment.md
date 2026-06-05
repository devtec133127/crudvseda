# ADR-006: Kubernetes als Zielbetriebsmodell

## Status

Accepted

## Kontext

Das System startet als Entwicklungsprojekt mit Docker Compose als lokaler Laufzeitumgebung. Docker Compose eignet sich hervorragend für die schnelle lokale Entwicklung: Ein einziger Befehl (`docker-compose up`) startet die vollständige Infrastruktur (PostgreSQL, Kafka) und alle Applikationskomponenten in einer reproduzierbaren Umgebung.

Für den produktiven Betrieb ist Docker Compose jedoch unzureichend: Es fehlen Mechanismen für automatisches Failover, horizontale Skalierung, Rolling Deployments, Health Management, Secret-Verwaltung und Netzwerksegmentierung. Docker Compose ist nicht für den Betrieb auf mehreren Hosts konzipiert und bietet keine API für programmatisches Management.

Als Architekturziel ist **Cloud- und Kubernetes-Readiness** explizit definiert. Das System soll so gebaut werden, dass es ohne strukturelle Änderungen in einer Kubernetes-Umgebung betrieben werden kann – on-premises, in einer Private Cloud oder bei einem Public Cloud Provider (AWS EKS, Azure AKS, Google GKE).

Die Entscheidung betrifft das **Zielbetriebsmodell**: Kubernetes ist die Produktionsplattform, Docker Compose bleibt das Werkzeug für lokale Entwicklung.

## Entscheidung

**Kubernetes** wird als Zielbetriebsmodell für alle produktiven Deployments festgelegt. Docker Compose bleibt ausschließlich für die lokale Entwicklungsumgebung.

Die Architekturentscheidung umfasst folgende Leitlinien:

**Anwendungsdesign für Kubernetes-Readiness:**
- Alle Services sind **zustandslos** (Stateless) – Zustände werden in PostgreSQL oder Kafka persistiert, nicht im Service-Speicher
- **Readiness und Liveness Probes** werden über Spring Boot Actuator (`/actuator/health`) bereitgestellt
- **Konfiguration erfolgt ausschließlich über Umgebungsvariablen** (12-Factor App, Faktor III) – keine hardcodierten Verbindungszeichenfolgen
- **Graceful Shutdown** ist in Spring Boot konfiguriert, um laufende Requests bei Pod-Termination abzuschließen
- **Container-Images** werden nach dem Prinzip „ein Image, mehrere Umgebungen" gebaut – Umgebungsspezifika kommen aus der Kubernetes-Konfiguration (ConfigMaps, Secrets)

**Deployment-Strategie:**
- **Helm Charts** für die Paketierung und Versionierung aller Kubernetes-Ressourcen (Deployments, Services, Ingress, ConfigMaps, Secrets)
- **Rolling Update** als Standard-Deployment-Strategie für Zero-Downtime-Deployments
- **Horizontal Pod Autoscaler (HPA)** für lastbasierte Skalierung des Catalog Service und des AI Enrichment Service
- **Namespace-Trennung** für Umgebungsisolation (Development, Staging, Production)

**Infrastrukturbetrieb:**
- PostgreSQL und Kafka werden als managed Services von einem Cloud-Provider bezogen (AWS RDS, Confluent Cloud, MSK) oder als Kubernetes-native Operatoren betrieben (Zalando PostgreSQL Operator, Strimzi Kafka Operator)
- Das Observability-Stack (Loki, Grafana, Prometheus) wird als eigenständiges Namespace-Deployment gemäß ADR-005 betrieben

## Konsequenzen

### Vorteile

- **Produktionsfähigkeit:** Kubernetes bietet alle Mechanismen für den zuverlässigen Betrieb verteilter Systeme: automatisches Failover, Self-Healing, Rolling Updates, Resource Management und Netzwerksegmentierung.
- **Horizontale Skalierung:** Einzelne Services können unabhängig skaliert werden. Der ressourcenintensive AI Enrichment Service kann auf GPU-Knoten platziert werden, ohne andere Services zu beeinflussen.
- **Cloud-Agnostizität:** Kubernetes läuft auf allen großen Cloud-Providern (AWS, Azure, GCP) sowie on-premises. Das System ist nicht an einen Provider gebunden.
- **Deklaratives Infrastructure-as-Code:** Helm Charts und Kubernetes-Manifeste beschreiben den Zielzustand des Systems deklarativ. Der Ist-Zustand wird kontinuierlich mit dem Soll-Zustand abgeglichen (Reconciliation Loop).
- **Sicherheitsmodel:** Kubernetes bietet RBAC, Network Policies, Pod Security Standards und Secrets-Management als Basiskonzepte. Diese sind in Docker Compose nicht verfügbar.
- **Ökosystem:** Ein reiches Ökosystem an Operators, Service Meshes (Istio, Linkerd), GitOps-Tools (ArgoCD, Flux) und CI/CD-Integrationen ist nativ auf Kubernetes ausgerichtet.

### Nachteile

- **Operative Komplexität:** Kubernetes ist signifikant komplexer als Docker Compose. Ein funktionsfähiger Cluster erfordert Expertise in Cluster-Management, Netzwerkkonzepten (CNI), Storage (CSI) und Sicherheitsmodellen.
- **Ressourcen-Overhead:** Kubernetes benötigt eigene Systemressourcen (Control Plane, CoreDNS, kube-proxy). Für kleinste Deployments ist der Overhead messbar.
- **Langsamere Feedback-Zyklen:** Änderungen im Kubernetes-Kontext erfordern Image-Builds und Deployments. Tools wie Skaffold oder Tilt mildern dies, sind aber ein zusätzlicher Lernaufwand.
- **Lokale Entwicklung bleibt Docker Compose:** Die Divergenz zwischen lokaler Entwicklung (Docker Compose) und Produktion (Kubernetes) birgt das Risiko, dass Produktionsfehler lokal nicht reproduzierbar sind. Minikube oder Kind als lokale Kubernetes-Cluster sind ein Kompromiss.

## Betrachtete Alternativen

**Docker Compose für Produktion:** Technisch möglich für sehr einfache Single-Host-Deployments. Kein automatisches Failover, keine horizontale Skalierung, keine rollingUpdates. Nicht geeignet für ein Cloud-ready Referenzprojekt.

**Serverless (AWS Lambda, Azure Functions):** Hervorragend für ereignisgetriebene, zustandslose Workloads. Allerdings: Cold-Start-Latenz für Spring Boot unakzeptabel ohne GraalVM-Kompilierung, starke Cloud-Provider-Bindung, und Kafka-Konsumenten sind in Serverless-Modellen konzeptionell unnatürlich.

**VM-basierter Betrieb mit Ansible/Terraform:** Etabliert und gut verstanden. Kein automatisches Scaling, keine Container-native Ressourcenisolation, höherer operativer Aufwand für Updates. Nicht geeignet als Zielplattform für ein modernes Cloud-native System.

**Managed Container Services (AWS ECS, Azure Container Apps):** Reduzieren operative Kubernetes-Komplexität deutlich. Erzeugen aber starke Cloud-Provider-Bindung. Als Kompromiss für Teams ohne Kubernetes-Expertise diskutierbar, aber widerspricht dem Architekturziel der Cloud-Agnostizität.

**Nomad (HashiCorp):** Leichtgewichtigere Alternative zu Kubernetes, besser für heterogene Workloads (Container und nicht-containerisierte Services). Deutlich kleineres Ökosystem; nicht der Industriestandard für Cloud-native Workloads.

## Referenzen

- Kubernetes Documentation: kubernetes.io/docs
- Fowler, Martin: *The Twelve-Factor App*, 12factor.net
- Burns, B. et al.: *Kubernetes: Up and Running*, O'Reilly, 3. Auflage, 2022
- Beda, J. et al.: *Kubernetes Patterns*, O'Reilly, 2. Auflage, 2023
- CNCF Landscape: landscape.cncf.io
- iSAQB Curriculum: Modul CLOUD (Cloud-native Architekturen, Container-Orchestrierung)
- Helm: helm.sh/docs
