# Decentral Library System

> Demo-Projekt zur Demonstration von Domain-Driven Design (DDD) und Event-Driven Architecture (EDA) am Beispiel eines
> dezentralen Bibliothekssystems.

## 🎯 Über das Projekt

Dieses System demonstriert die praktische Anwendung von:

- **Domain-Driven Design** mit expliziten Bounded Contexts
- **Event-Driven Architecture** mit Choreography-Pattern
- **Hexagonal Architecture** (Ports & Adapters)

Das System simuliert ein Fernleih-Netzwerk zwischen verschiedenen Bibliotheken.

## 📦 Bounded Contexts

Das System ist in vier fachliche Kontexte unterteilt:

- **Loan Context**: Verwaltung von Buchausleihen
- **Inventory Context**: Verwaltung des Buchbestands
- **Procurement Context**: Externe Buchbeschaffung
- **Payment Context**: Abwicklung von Gebühren

## 🎬 Implementierter Use Case

**UC1: Buch ausleihen (lokal vorhanden)**

1. User fordert ein Buch an
2. Inventory prüft Verfügbarkeit und reserviert ein Exemplar
3. Loan wird aktiviert
4. User kann das Buch abholen

_(Use Case 2: Externe Beschaffung ist in Vorbereitung)_

## 🛠️ Tech Stack

- Java 21
- Spring Boot
- Apache Kafka (Event Streaming)
- PostgreSQL
- Maven

## 🚀 Quick Start

```bash
# Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

# 1. Repository klonen
git clone https://github.com/dein-username/decentral-library-system.git
cd decentral-library-system

# 2. Infrastruktur starten (Kafka + PostgreSQL)
docker-compose up -d

# 3. Projekt bauen
mvn clean install

# 4. Anwendung starten
mvn spring-boot:run

# 5. Health Check
curl http://localhost:8080/actuator/health

# 6. UI is reachable under 
http://localhost:8080/demo/demo.html
```

## 📡 API Beispiele

### Loan anfordern (Endpoint 1)

```bash
curl -X POST http://localhost:8083/loans \
  -H "Content-Type: application/json" \
  -d '{
    "id": "fd2fab66-8a6a-11f0-829e-005056bb85fb",
    "bookId": "5a47647d-4a22-4967-9d1d-9c9d6e7b66c4"
  }'
```

### Loan anfordern (Endpoint 2)

```bash
curl -X POST http://localhost:8083/lending/loans/request \
  -H "Content-Type: application/json" \
  -d '{
    "bookTitle": "Testbuch",
    "userId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

## 🔄 Event Flow

```
User → LoanRequested → Inventory → CopyReserved → Loan → LoanActivated
```

_(Detailliertes Sequence-Diagramm folgt)_

## 🏗️ Architektur-Details

### Aggregate Design

Jeder Bounded Context folgt dem DDD-Aggregate-Pattern:

- **Aggregate Roots** schützen Geschäftsregeln
- **Value Objects** für unveränderliche Konzepte (ISBN, IDs)
- **Domain Events** für Bounded Context Kommunikation

**Beispiel-Implementierungen:**

- `Loan` Aggregate im Loan Context
- Alternative Entity-Modellierung im `inventory/domain/alternative` Package (zu Demonstrationszwecken)

### Event-Driven Communication

Bounded Contexts kommunizieren ausschließlich über Domain Events:

- Lose Kopplung zwischen Kontexten
- Choreography statt Orchestration
- Event Sourcing-ready Design

## 🤔 Design-Entscheidungen

### Warum InventoryCopy als separates Aggregate?

Die alternative Modellierung (Book-Aggregate mit BookCopy-Entities) wurde zugunsten kleinerer, fokussierter Aggregates
verworfen:

- ✅ Bessere Skalierbarkeit bei gleichzeitigen Zugriffen
- ✅ Vermeidung von Optimistic Locking Konflikten
- ✅ Klare Bounded Context Grenzen

_(Siehe `inventory/domain/alternative` für Vergleichsimplementierung)_

## 📚 Weiterführende Informationen

- [TODO: Event Catalog]
- [TODO: C4 Diagramme]
- [TODO: Setup Guide]

---

## Status

🚧 **Work in Progress** - Dieses Projekt dient als Lern- und Demonstrationsprojekt.