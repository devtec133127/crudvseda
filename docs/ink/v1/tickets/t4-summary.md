# T4 – Implementierungszusammenfassung

## Neue Dateien

| Datei | Zweck |
|-------|-------|
| `static/catalog/catalog.css` | Gemeinsames Stylesheet für T4 + T5 (Variablen, Basis-Klassen, Kacheln, Detail-Panel, Tabelle) |
| `static/catalog/search.html` | Suchseite – Struktur mit allen IDs als JS-Kontrakt |
| `static/catalog/search.js` | Suchlogik – `doSearch`, `renderResults`, `selectBook`, `showDetail`, `addToCatalog` |

Keine bestehende Datei wurde verändert.

## Getroffene Annahmen

**`catalog.css` enthält bereits Klassen für T5.** Das Stylesheet wurde direkt mit den Tabellen- und Delete-Button-Klassen (`.catalog-table`, `.delete-button`, `.table-wrapper`) vorbereitet, damit `manage.html` (T5) dieselbe Datei einbinden kann ohne CSS-Nacharbeit.

**XSS-Escaping.** Alle Inhalte aus der OpenLibrary-API werden vor dem Einfügen in den DOM escapt (`escapeHtml` für Texte, `escapeAttr` für Attributwerte). Das bestehende `demo.js` tut dies nicht – hier wurde bewusst eine sicherere Variante gewählt, weil OpenLibrary externe Daten liefert.

**Kein `placeholder.png` angelegt.** Bücher ohne Cover erhalten `visibility: hidden` auf dem `<img>`-Tag. Die umgebende `.book-card-cover`-Box (140px Höhe, grauer Hintergrund) bleibt sichtbar und verhindert Layout-Sprünge. Das `onerror`-Attribut setzt zusätzlich die Klasse `.no-cover`, falls ein Bild-URL existiert aber das Bild nicht lädt.

**`API_BASE = ''` (relativer Pfad).** Kein hardcodierter `localhost`-Host. Die Fetch-Calls lauten `/catalog/search?q=...` und `/catalog/books` – funktioniert auf jedem Port und Host.

**Keyboard-Zugänglichkeit.** Buch-Kacheln sind mit `tabindex="0"` und einem `keydown`-Handler (Enter/Space) versehen, da sie `<div>` statt `<button>` sind.

## So kann ich es testen

### 1. Anwendung starten

```bash
docker-compose up -d
mvn spring-boot:run
```

### 2. Seite aufrufen

```
http://localhost:8080/catalog/search.html
```

### 3. Goldener Pfad testen

1. „Clean Code" in das Suchfeld eingeben, Enter drücken
2. Trefferkacheln erscheinen mit Cover, Titel, Autor
3. Eine Kachel anklicken → blauer Rahmen, Detailansicht öffnet sich
4. „In Katalog übernehmen" klicken → grüne Bestätigung, Button bleibt deaktiviert

### 4. Edge Cases prüfen

| Szenario | Erwartetes Verhalten |
|----------|---------------------|
| Leeres Suchfeld + Suchen | Nichts passiert |
| Suchbegriff ohne Treffer | „Keine Treffer für »…«" |
| Buch ohne Cover | Graue Fläche, kein kaputtes Bild-Icon |
| Zweimal „Übernehmen" klicken | Nicht möglich – Button bleibt nach Erfolg disabled |
| Navigation zu „Mein Katalog" | Link öffnet `manage.html` (noch nicht vorhanden – 404 bis T5) |

### 5. Browser-Konsole

Beim Laden und nach jeder Interaktion: keine roten Fehler in der Konsole.

### 6. Bestehende Demo unverändert

```
http://localhost:8080/demo/demo.html
```

Weiterhin erreichbar und funktionsfähig.
