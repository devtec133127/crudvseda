# T5 – Implementierungszusammenfassung

## Neue Dateien

| Datei | Zweck |
|-------|-------|
| `static/catalog/manage.html` | Verwaltungsseite – Tabellenstruktur mit 3 ID-Ankerpunkten |
| `static/catalog/manage.js` | Seitenlogik – `loadCatalog`, `renderTable`, `removeBook`, `formatDate` |

## Geänderte Dateien

Keine. `catalog.css` wurde nicht angefasst.

## Getroffene Annahmen

**`innerHTML` für die Statusmeldung bei leerem Katalog.** Das Ticket fordert einen klickbaren Link in der Statusmeldung. `textContent` reicht dafür nicht – `statusMsg.innerHTML` wird gezielt nur für diesen einen Fall mit einem harcodierten relativen `<a href="search.html">`-Link gesetzt. Kein Nutzereingabe-Inhalt fließt in diesen `innerHTML`-Aufruf ein, daher kein XSS-Risiko.

**Fehler beim Löschen lässt Tabelle sichtbar.** Wenn `DELETE` fehlschlägt, zeigt `statusMsg` die Fehlermeldung an, aber `tableContainer` bleibt eingeblendet (`classList.remove('hidden')` wird explizit aufgerufen). So kann der Nutzer es erneut versuchen ohne die Seite neu laden zu müssen.

**Click-Handler direkt auf dem Button, nicht per `data-*`-Delegation.** Die Buttons bekommen ihren Handler beim Rendern direkt per `addEventListener` – konsistent mit `search.js`. Der `data-id`/`data-title`-Ansatz im `innerHTML` dient nur als Datenspeicher; das Event liest die Werte aus `dataset`.

## So kann ich es testen

### 1. Anwendung starten

```bash
docker-compose up -d
mvn spring-boot:run
```

### 2. Seite aufrufen

```
http://localhost:8080/catalog/manage.html
```

### 3. Leerer Katalog

Vor dem Einfügen von Büchern: Seite zeigt  
„Noch keine Bücher im Katalog. Zur Buchsuche →"  
Link führt zu `search.html`.

### 4. Buch hinzufügen und in Katalog anzeigen

```bash
curl -s -X POST http://localhost:8080/catalog/books \
  -H "Content-Type: application/json" \
  -d '{"isbn":"9780132350884","title":"Clean Code","author":"Robert C. Martin","coverUrl":"https://covers.openlibrary.org/b/id/8432581-M.jpg","openLibraryKey":"/works/OL1820750W"}'
```

Seite neu laden → Buch erscheint in der Tabelle mit Cover, Titel, Autor, ISBN und Datum (z. B. `04.06.2026`).

### 5. Buch entfernen

„Entfernen"-Button klicken → Bestätigungsdialog mit Buchtitel erscheint  
→ Bestätigen → Tabelle aktualisiert sich (Buch weg)  
→ Abbrechen → Buch bleibt erhalten

### 6. Buch ohne Cover

```bash
curl -s -X POST http://localhost:8080/catalog/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Buch ohne Cover","author":"Unbekannt"}'
```

In der Tabelle: keine kaputte Bild-Ikone, die Cover-Zelle bleibt leer.

### 7. Navigation prüfen

- „Mein Katalog" ist fett/blau unterstrichen (aktiver Nav-Link)
- „Buchsuche" führt zu `search.html`

### 8. Bestehende Seiten unverändert

```
http://localhost:8080/catalog/search.html  → funktioniert
http://localhost:8080/demo/demo.html       → funktioniert
```
