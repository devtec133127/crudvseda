// ─────────────────────────────────────────────────────────────────────────────
// Library Catalog – Katalogverwaltung
// Vanilla JS, Fetch API, keine externen Abhängigkeiten
// ─────────────────────────────────────────────────────────────────────────────

const API_BASE = '';

// DOM-Referenzen
const statusMsg        = document.getElementById('statusMsg');
const tableContainer   = document.getElementById('tableContainer');
const catalogTableBody = document.getElementById('catalogTableBody');

// ─── Initialisierung ──────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    loadCatalog();
});

// ─── Katalog laden ────────────────────────────────────────────────────────────

async function loadCatalog() {
    showStatus('Katalog wird geladen …');
    tableContainer.classList.add('hidden');

    try {
        const response = await fetch(`${API_BASE}/catalog/books`);
        if (!response.ok) {
            throw new Error(`Serverfehler (${response.status})`);
        }
        const books = await response.json();

        if (books.length === 0) {
            statusMsg.innerHTML =
                'Noch keine Bücher im Katalog. ' +
                '<a href="search.html">Zur Buchsuche →</a>';
            statusMsg.classList.remove('hidden');
        } else {
            statusMsg.classList.add('hidden');
            tableContainer.classList.remove('hidden');
            renderTable(books);
        }
    } catch (err) {
        showStatus(`Katalog konnte nicht geladen werden: ${err.message}`);
    }
}

// ─── Tabelle rendern ──────────────────────────────────────────────────────────

function renderTable(books) {
    catalogTableBody.innerHTML = '';

    books.forEach(book => {
        const tr = document.createElement('tr');

        const hasCover = !!book.coverUrl;
        const coverSrc = hasCover ? escapeAttr(book.coverUrl) : '';
        const coverStyle = hasCover ? '' : 'visibility:hidden';

        tr.innerHTML = `
            <td>
                <img class="cover-thumb"
                     src="${coverSrc}"
                     alt="${escapeAttr(book.title || '')}"
                     style="${coverStyle}"
                     onerror="this.style.visibility='hidden'">
            </td>
            <td>${escapeHtml(book.title || '–')}</td>
            <td>${escapeHtml(book.author || '–')}</td>
            <td>${escapeHtml(book.isbn || '–')}</td>
            <td>${formatDate(book.addedAt)}</td>
            <td>
                <button class="delete-button"
                        data-id="${escapeAttr(book.id)}"
                        data-title="${escapeAttr(book.title || '')}">
                    Entfernen
                </button>
            </td>
        `;

        tr.querySelector('.delete-button').addEventListener('click', (e) => {
            const btn = e.currentTarget;
            removeBook(btn.dataset.id, btn.dataset.title);
        });

        catalogTableBody.appendChild(tr);
    });
}

// ─── Buch entfernen ───────────────────────────────────────────────────────────

async function removeBook(id, title) {
    const confirmed = window.confirm(`Buch »${title}« wirklich entfernen?`);
    if (!confirmed) return;

    try {
        const response = await fetch(`${API_BASE}/catalog/books/${id}`, {
            method: 'DELETE'
        });

        if (response.status === 204) {
            await loadCatalog();
        } else {
            throw new Error(`Serverfehler (${response.status})`);
        }
    } catch (err) {
        showStatus(`Entfernen fehlgeschlagen: ${err.message}`);
        tableContainer.classList.remove('hidden');
    }
}

// ─── Hilfsfunktionen ──────────────────────────────────────────────────────────

function showStatus(text) {
    statusMsg.textContent = text;
    statusMsg.classList.remove('hidden');
}

function formatDate(isoString) {
    if (!isoString) return '–';
    try {
        return new Date(isoString).toLocaleDateString('de-DE', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    } catch {
        return '–';
    }
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
