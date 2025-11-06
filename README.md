Beispiel Anfrage (createLoan):

curl -X POST http://localhost:8081/loans -H "Content-Type: application/json" -d '{"id": "
fd2fab66-8a6a-11f0-829e-005056bb85fb", "bookId": "5a47647d-4a22-4967-9d1d-9c9d6e7b66c4"}'

curl -X POST http://localhost:8083/lending/loans/request -H "Content-Type: application/json" -d '{"bookTitle": "
Testbuch", "userId": "123e4567-e89b-12d3-a456-426614174000"}'

########## INDEMPOTENZ ###############

- regelmäßiger Clean-Up Job verhindert unendliches Wachstum
- Archivierung für Audits in Betracht ziehen anstatt löschen
- Spring Boot Scheduled Job - Kubernetes Cron Job
- Indexe setzen, insbesondere für clean up job
- prüfen und setzen in einer Transaktion
- Metriken & Alerts hilfreich (duplicate_detected, duplicate_processed), um falsch konfigurierte Producer zu finden (
  praktisches Beispiel damals WBCI 20000 Anfragen

########## OUTBOX ##############

- Variante 1: App schreibt Outbox Row - Poller Job liest unversendete Zeilen und publiziert
    - Pro: einfach, keine zusätzlichen Komponenten
    - Contra: Poll latency, eigene Robustheit / Skalierung nötig
    - Use Case: geeignet für kleinere bis mittlere Systeme
- Variante 2: Debezium liest DB-WAL, transformiert Outbox-Rows zu Kafka Events - Log Tailing / CDC (Debezium + Kafka
  Connect + Outbox Event Router)
    - Pro: sehr robust, skaliert gut, keine Poller-Boilerplate, gute exactly-once/ordering Optionen bei Kafka
    - Contra: Infrastukturaufwand
    - Use Case: geeignet für größere Setups mit Kafka / viele Services
- Variante 3: verwende eine Library, die Outbox + Poller kapselt (Spring Outbox, gruelbox/transaction-outbox)
    - Pro: weniger Boilerplate, Spring-native Integrationen
    - Contra: Abhängigkeit von Library-Design, manchmal weniger flexibel
    - Use Case: geeignet für kleinere bis mittlere Systeme

- Transaction Boundaries sind heilig - schreibe Outbox Row innerhalb der gleichen DB-Transaktion, die den Domain-State
  ändert
- At-least-once delivery designen - Outbox garantiert meist at-least-once. Daher muss Consumer indempotent sein
- Poller sollte in Batches (100 - 1000) lesen, damit sinkt der Durchsatz-Overhead und die Broker Effizienz steigt
- Exponential Backoff + DLQ - bei Broker Fehlern retryen, andernfals row markieren und DLQ verschieben
- Payload Schema versionieren
- Monitoring & Metrics - Metriken für outbox_rows_pending, publish_errors, publish_latency, Alert on growing backlog
- Clean-Up - alte erfolgreich veröffentlichte Zeilen regelmäßig archivieren oder löschen
- Test - simuliere crash zwischen DB commit und publish

# Polling-Concurrency (Praktische Patterns)

- SELECT ... FOR UPDATE SKIP LOCKED (Postgres) — erlaubt mehrere Poller gleichzeitig ohne Kollision.
- Optimistic update: UPDATE outbox SET status='SENDING' WHERE id IN (...) AND status='PENDING' RETURNING id — nur
  gewonnene rows send.
- Wenn DB kein SKIP LOCKED unterstützt, nutze single leader CronJob (Kubernetes CronJob) oder leader election.

# Fehlerfälle & Recovery

- Partial failure: DB committed, publish failed → retry or mark failed → alert.
- Publisher crash after publish but before marking SENT: Consumer must tolerate duplicates (idempotency). Consider
  writing a result back to DB after successful publish to avoid long windows.
- Debezium caveat: Debezium reads the commit log; ensure outbox rows are visible in WAL and configured transformations (
  Outbox Event Router) applied correctly.