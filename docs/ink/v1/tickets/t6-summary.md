# T6 – Implementierungszusammenfassung

## Neue Dateien

| Datei | Zweck |
|-------|-------|
| `static/catalog/index.html` | Landing Page mit zwei Feature-Kacheln, kein JS |

## Geänderte Dateien

| Datei | Änderung |
|-------|----------|
| `static/catalog/catalog.css` | `.feature-grid` und `.feature-card` am Ende ergänzt |
| `static/catalog/search.html` | Nav-Bar: `<a href="index.html">Startseite</a>` als erster Link |
| `static/catalog/manage.html` | Nav-Bar: `<a href="index.html">Startseite</a>` als erster Link |

`search.js` und `manage.js` wurden nicht verändert.

## Getroffene Annahmen

**Demo-Link mit Inline-Style.** Der EDA-Demo-Link am Ende von `index.html` nutzt `style="color: var(--text-muted)"` direkt im HTML, anstatt eine neue CSS-Klasse einzuführen. Für einen einmalig verwendeten Stil auf einer statischen Seite ist das pragmatisch und hält `catalog.css` schlank.

**Responsive Breakpoint für `.feature-grid` separat.** Der `@media (max-width: 600px)`-Block für `.feature-grid` wurde als eigener Block nach den Feature-Card-Klassen gesetzt – nicht in den bestehenden `@media (max-width: 480px)`-Block integriert – um die neuen Klassen klar beieinander zu halten.

## So kann ich es testen

### 1. Anwendung starten

```bash
docker-compose up -d
mvn spring-boot:run
```

### 2. Landing Page aufrufen

```
http://localhost:8080/catalog/index.html
```

Erwartetes Bild: Zwei Kacheln „Bücher suchen" und „Mein Katalog" nebeneinander, darunter der EDA-Demo-Link.

### 3. Kachel-Navigation prüfen

- Kachel „Bücher suchen" → führt zu `search.html`
- Kachel „Mein Katalog" → führt zu `manage.html`
- EDA-Demo-Link → führt zu `/demo/demo.html`

### 4. Hover-Effekt

Im Browser: Maus über eine Kachel bewegen → Kachel hebt sich leicht, Rahmen wird blau.

### 5. Nav-Bar auf Unterseiten

```
http://localhost:8080/catalog/search.html
```
Nav zeigt: `Startseite | Buchsuche (aktiv) | Mein Katalog`

```
http://localhost:8080/catalog/manage.html
```
Nav zeigt: `Startseite | Buchsuche | Mein Katalog (aktiv)`

„Startseite" ist auf beiden Seiten ohne `.active`-Styling.

### 6. Responsive Layout

Browser-DevTools öffnen, Viewport auf < 600px setzen → Kacheln stacken vertikal.

### 7. Bestehende Seiten unverändert

```
http://localhost:8080/demo/demo.html   → HTTP 200, funktionsfähig
```
