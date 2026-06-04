// ─────────────────────────────────────────────────────────────────────────────
// Library Catalog – Buchsuche
// Vanilla JS, Fetch API, keine externen Abhängigkeiten
// ─────────────────────────────────────────────────────────────────────────────

const API_BASE = '';

let currentResults = [];
let selectedIndex = null;

// DOM-Referenzen
const searchForm   = document.getElementById('searchForm');
const searchInput  = document.getElementById('searchInput');
const searchBtn    = document.getElementById('searchBtn');
const statusMsg    = document.getElementById('statusMsg');
const resultsGrid  = document.getElementById('resultsGrid');
const detailPanel  = document.getElementById('detailPanel');
const detailCover  = document.getElementById('detailCover');
const detailTitle  = document.getElementById('detailTitle');
const detailAuthor = document.getElementById('detailAuthor');
const detailIsbn   = document.getElementById('detailIsbn');
const addBtn       = document.getElementById('addBtn');
const feedbackMsg  = document.getElementById('feedbackMsg');

// ─── Initialisierung ──────────────────────────────────────────────────────────

searchForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    await doSearch();
});

addBtn.addEventListener('click', async () => {
    await addToCatalog();
});

// ─── Suche ───────────────────────────────────────────────────────────────────

async function doSearch() {
    const query = searchInput.value.trim();
    if (!query) return;

    resetUI();
    showStatus('Suche läuft …');
    searchBtn.disabled = true;

    try {
        const response = await fetch(`${API_BASE}/catalog/search?q=${encodeURIComponent(query)}`);
        if (!response.ok) {
            throw new Error(`Serverfehler (${response.status})`);
        }
        const results = await response.json();
        currentResults = results;

        if (results.length === 0) {
            showStatus(`Keine Treffer für »${escapeHtml(query)}«`);
        } else {
            hideStatus();
            renderResults(results);
        }
    } catch (err) {
        showStatus(`Suche fehlgeschlagen: ${err.message}`);
    } finally {
        searchBtn.disabled = false;
    }
}

// ─── Trefferliste rendern ─────────────────────────────────────────────────────

function renderResults(results) {
    resultsGrid.innerHTML = '';
    resultsGrid.classList.remove('hidden');

    results.forEach((book, index) => {
        const card = document.createElement('div');
        card.className = 'book-card';
        card.setAttribute('role', 'button');
        card.setAttribute('tabindex', '0');

        const hasCover = !!book.coverUrl;
        card.innerHTML = `
            <div class="book-card-cover">
                <img src="${hasCover ? escapeAttr(book.coverUrl) : ''}"
                     alt="${escapeAttr(book.title || '')}"
                     class="${hasCover ? '' : 'no-cover'}"
                     onerror="this.classList.add('no-cover')">
            </div>
            <div class="book-card-title">${escapeHtml(book.title || '–')}</div>
            <div class="book-card-author">${escapeHtml(book.author || '–')}</div>
        `;

        card.addEventListener('click', () => selectBook(index));
        card.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') selectBook(index);
        });

        resultsGrid.appendChild(card);
    });
}

// ─── Treffer auswählen ────────────────────────────────────────────────────────

function selectBook(index) {
    document.querySelectorAll('.book-card').forEach(c => c.classList.remove('selected'));
    const cards = document.querySelectorAll('.book-card');
    if (cards[index]) cards[index].classList.add('selected');

    selectedIndex = index;

    // Feedback-Bereich zurücksetzen
    feedbackMsg.textContent = '';
    feedbackMsg.className = 'feedback-msg hidden';

    showDetail(currentResults[index]);
}

// ─── Detailansicht ────────────────────────────────────────────────────────────

function showDetail(book) {
    detailPanel.classList.remove('hidden');

    if (book.coverUrl) {
        detailCover.src = book.coverUrl;
        detailCover.style.visibility = 'visible';
    } else {
        detailCover.src = '';
        detailCover.style.visibility = 'hidden';
    }
    detailCover.alt = book.title || '';

    detailTitle.textContent = book.title || '–';
    detailAuthor.textContent = book.author ? `Autor: ${book.author}` : '–';
    detailIsbn.textContent   = book.isbn   ? `ISBN: ${book.isbn}`   : '–';

    addBtn.disabled = false;
    addBtn.textContent = 'In Katalog übernehmen';

    detailPanel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// ─── Buch in Katalog übernehmen ───────────────────────────────────────────────

async function addToCatalog() {
    if (selectedIndex === null) return;
    const book = currentResults[selectedIndex];

    addBtn.disabled = true;
    addBtn.textContent = 'Wird gespeichert …';

    try {
        const response = await fetch(`${API_BASE}/catalog/books`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                isbn:           book.isbn,
                title:          book.title,
                author:         book.author,
                coverUrl:       book.coverUrl,
                openLibraryKey: book.openLibraryKey
            })
        });

        if (response.status === 201) {
            showFeedback('success', `✓ »${escapeHtml(book.title)}« wurde zum Katalog hinzugefügt.`);
            // Button bleibt deaktiviert – kein Doppel-Eintrag möglich
        } else if (response.status === 400) {
            const data = await response.json().catch(() => ({}));
            showFeedback('error', data.error || 'Ungültige Anfrage.');
            resetAddBtn();
        } else {
            throw new Error(`Serverfehler (${response.status})`);
        }
    } catch (err) {
        showFeedback('error', `Fehler beim Speichern: ${err.message}`);
        resetAddBtn();
    }
}

// ─── Hilfsfunktionen ──────────────────────────────────────────────────────────

function resetUI() {
    resultsGrid.classList.add('hidden');
    resultsGrid.innerHTML = '';
    detailPanel.classList.add('hidden');
    feedbackMsg.className = 'feedback-msg hidden';
    currentResults = [];
    selectedIndex = null;
}

function showStatus(text) {
    statusMsg.textContent = text;
    statusMsg.classList.remove('hidden');
}

function hideStatus() {
    statusMsg.classList.add('hidden');
}

function showFeedback(type, message) {
    feedbackMsg.innerHTML = message;
    feedbackMsg.className = `feedback-msg ${type}`;
}

function resetAddBtn() {
    addBtn.disabled = false;
    addBtn.textContent = 'In Katalog übernehmen';
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = String(str);
    return div.innerHTML;
}

function escapeAttr(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}
