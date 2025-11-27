// =============================================================================
// Demo UI für Event-Driven Loan System
// Empfängt Server-Sent Events und zeigt Event-Timeline in Echtzeit
// =============================================================================

const API_BASE = 'http://localhost:8080/api/demo';
let eventSource = null;
let currentLoanId = null;  // ← HINZUGEFÜGT!

// =============================================================================
// Button Click Handler - Startet den Loan Request
// =============================================================================

document.getElementById('createLoanBtn').addEventListener('click', async () => {
    console.log('=== BUTTON CLICKED ===');

    // Reset UI
    resetUI();

    // Button deaktivieren während Request läuft
    const button = document.getElementById('createLoanBtn');
    button.disabled = true;
    button.textContent = '⏳ Erstelle Loan Request...';

    try {
        console.log('Sending POST request to:', `${API_BASE}/loans`);

        const bookTitle = document.getElementById('bookTitleInput').value.trim();
        // POST Request zum Backend
        const response = await fetch(`${API_BASE}/loans`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: '518aeace-387a-4a16-a0b8-b6d6fa9e8bc3',
                bookTitle: bookTitle
            })
        });

        console.log('Response status:', response.status);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const loan = await response.json();
        console.log('=== LOAN CREATED ===', loan);

        currentLoanId = loan.id;

        // Zeige Initial Response
        displayInitialResponse(loan);

        // Öffne SSE Stream für Live-Updates
        subscribeToEvents(loan.id);

    } catch (error) {
        console.error('=== ERROR ===', error);
        showError('Fehler beim Erstellen des Loan Requests: ' + error.message);
    } finally {
        // Button wieder aktivieren
        button.disabled = false;
        button.textContent = '🚀 Neuen Loan Request erstellen';
    }
});

// =============================================================================
// SSE Event Subscription - Öffnet Stream für Live-Updates
// =============================================================================

function subscribeToEvents(loanId) {
    console.log('=== OPENING SSE STREAM ===');
    console.log('Loan ID:', loanId);
    console.log('SSE URL:', `${API_BASE}/loans/${loanId}/events`);

    // Schließe vorherige Verbindung falls vorhanden
    if (eventSource) {
        console.log('Closing previous EventSource');
        eventSource.close();
    }

    // Öffne neue SSE-Verbindung
    eventSource = new EventSource(`${API_BASE}/loans/${loanId}/events`);
    console.log('EventSource created, readyState:', eventSource.readyState);

    // Tracking für empfangene Events
    const receivedEvents = {
        payment: false,
        inventory: false
    };
    const startTime = Date.now();

    // Event-Listener für verschiedene Event-Typen

    // 1. Loan Created (optional)
    eventSource.addEventListener('loan-created', (e) => {
        console.log('=== SSE EVENT: loan-created ===');
        console.log('Raw data:', e.data);
        const data = JSON.parse(e.data);
        console.log('Parsed data:', data);
    });

    // 2. Payment Completed
    eventSource.addEventListener('payment-captured', (e) => {
        console.log('=== SSE EVENT: payment-captured ===');
        console.log('Raw data:', e.data);

        try {
            const data = JSON.parse(e.data);
            console.log('Parsed data:', data);

            const elapsed = Date.now() - startTime;

            displayEvent('payment', {
                title: 'Payment Service',
                message: data.message || 'Payment verarbeitet',
                details: [
                    `Transaction-ID: ${data.transactionId || 'N/A'}`,
                    `Betrag: ${data.amount ? formatCurrency(data.amount) : 'N/A'}`
                ],
                elapsed: data.elapsedMs || elapsed
            });

            receivedEvents.payment = true;
            console.log('Payment event processed. Received events:', receivedEvents);
            checkCompletion(receivedEvents);
        } catch (error) {
            console.error('Error processing payment event:', error);
        }
    });

    eventSource.addEventListener('procurement-initiated', (e) => {
        console.log('=== SSE EVENT: procurement-initiated ===');
        console.log('Raw data:', e.data);

        try {
            const data = JSON.parse(e.data);
            console.log('Parsed data:', data);

            const elapsed = Date.now() - startTime;

            displayEvent('procurement', {
                title: 'Procurement Service - Verfügbarkeit geprüft',
                message: data.message || 'Buch extern angefragt',
                details: [
                    //`Reservation-ID: ${data.reservationId || 'N/A'}`,
                    `Artikel: ${data.article || data.bookTitle || 'Buch'}`
                ],
                elapsed: data.elapsedMs || elapsed
            });

            receivedEvents.inventory = true;
            console.log('Inventory event processed. Received events:', receivedEvents);
            checkCompletion(receivedEvents);
        } catch (error) {
            console.error('Error processing inventory event:', error);
        }
    });

    eventSource.addEventListener('book-externally-ordered', (e) => {
        console.log('=== SSE EVENT: book-externally-ordered ===');
        console.log('Raw data:', e.data);

        try {
            const data = JSON.parse(e.data);
            console.log('Parsed data:', data);

            const elapsed = Date.now() - startTime;

            displayEvent('procurement', {
                title: 'Procurement Service - beauftragt',
                message: data.message || 'Buch extern beauftragt',
                details: [
                    //`Reservation-ID: ${data.reservationId || 'N/A'}`,
                    `Artikel: ${data.article || data.bookTitle || 'Buch'}`
                ],
                elapsed: data.elapsedMs || elapsed
            });

            receivedEvents.inventory = true;
            console.log('Inventory event processed. Received events:', receivedEvents);
            checkCompletion(receivedEvents);
        } catch (error) {
            console.error('Error processing inventory event:', error);
        }
    });

    // 3. Inventory Reserved
    eventSource.addEventListener('book-reserved', (e) => {
        console.log('=== SSE EVENT: book-reserved ===');
        console.log('Raw data:', e.data);

        try {
            const data = JSON.parse(e.data);
            console.log('Parsed data:', data);

            const elapsed = Date.now() - startTime;

            displayEvent('inventory', {
                title: 'Inventory Service',
                message: data.message || 'Buch reserviert',
                details: [
                    `Reservation-ID: ${data.reservationId || 'N/A'}`,
                    `Artikel: ${data.article || data.bookTitle || 'Buch'}`
                ],
                elapsed: data.elapsedMs || elapsed
            });

            receivedEvents.inventory = true;
            console.log('Inventory event processed. Received events:', receivedEvents);
            checkCompletion(receivedEvents);
        } catch (error) {
            console.error('Error processing inventory event:', error);
        }
    });

    // 4. Loan Finalized (optional)
    eventSource.addEventListener('loan-finalized', (e) => {
        console.log('=== SSE EVENT: loan-finalized ===');
        console.log('Raw data:', e.data);

        const data = JSON.parse(e.data);

        showFinalStatus({
            message: data.message || 'Loan abgeschlossen',
            elapsed: data.elapsedMs
        });

        // Schließe SSE-Verbindung
        eventSource.close();
        eventSource = null;
    });

    eventSource.addEventListener('analytics', (e) => {
        const data = JSON.parse(e.data);
        displayEvent(data.type, {
            title: data.type,
            elapsed: data.elapsedMs,
            message: data.message || 'Analysis',
            details: [],
        });
    })

    eventSource.addEventListener('notification', (e) => {
        const data = JSON.parse(e.data);
        displayEvent(data.type, {
            title: data.type,
            elapsed: data.elapsedMs,
            message: data.message || 'Notification',
            details: [],
        });
    })

    eventSource.addEventListener('fraud', (e) => {
        const data = JSON.parse(e.data);
        displayEvent(data.type, {
            title: data.type,
            elapsed: data.elapsedMs,
            message: data.message || 'Fraud Check',
            details: [],
        });
    })

    eventSource.addEventListener('payment-finished', (e) => {
        const data = JSON.parse(e.data);
        displayEvent(data.type, {
            title: data.type,
            elapsed: data.elapsedMs,
            message: data.message || 'Payment finished',
            details: [],
        });
    })

    // Error Handling
    eventSource.onerror = (error) => {
        console.error('=== SSE ERROR ===', error);
        console.log('EventSource readyState:', eventSource.readyState);

        if (eventSource.readyState === EventSource.CLOSED) {
            console.log('SSE connection CLOSED');
            showError('Verbindung zum Server verloren');
        } else if (eventSource.readyState === EventSource.CONNECTING) {
            console.log('SSE RECONNECTING...');
        }
    };

    // Connection opened
    eventSource.onopen = () => {
        console.log('=== SSE CONNECTION OPENED ===');
        console.log('EventSource readyState:', eventSource.readyState);
    };

    // Fallback: Lausche auf ALLE Events (Debug)
    eventSource.onmessage = (e) => {
        console.log('=== SSE GENERIC MESSAGE (no event type) ===');
        console.log('Data:', e.data);
    };
}

// =============================================================================
// UI Display Functions
// =============================================================================

/**
 * Zeigt die initiale Response vom Loan Service
 */
function displayInitialResponse(loan) {
    console.log('=== DISPLAY INITIAL RESPONSE ===');
    console.log('Loan data:', loan);

    const container = document.getElementById('initialResponse');
    const content = container.querySelector('.response-content');

    if (!container || !content) {
        console.error('Initial response containers not found!');
        return;
    }

    const timestamp = new Date().toLocaleTimeString('de-DE', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        fractionalSecondDigits: 3
    });

    content.innerHTML = `
        <div style="display: grid; grid-template-columns: 1fr 2fr; gap: 12px;">

          <!-- Zeile 1 -->
          <div>
            <p><strong>Status:</strong>
              <span class="status-badge status-pending">
                ${loan.status || 'PENDING'}
              </span>
            </p>
          </div>

          <div>
            <p><strong>Loan-ID:</strong> <code>${loan.id}</code></p>
          </div>

          <!-- Zeile 2 – über beide Spalten -->
          <div style="grid-column: 1 / 3;">
            <p><strong>⏱️ Empfangen:</strong> ${timestamp}</p>
          </div>

        </div>
    `;

    container.classList.remove('hidden');
    document.getElementById('timeline').classList.remove('hidden');

    console.log('Initial response displayed');

    // Smooth fade-in
    setTimeout(() => container.classList.add('visible'), 10);
}

/**
 * Zeigt ein Event in der Timeline - UNTEREINANDER!
 */
function displayEvent(type, data) {
    console.log('=== DISPLAY EVENT ===');
    console.log('Type:', type);
    console.log('Data:', data);

    const eventsContainer = document.getElementById('events');

    // Sicherstellen dass Container existiert
    if (!eventsContainer) {
        console.error('❌ Events container (#events) not found in HTML!');
        alert('FEHLER: Events container nicht gefunden! Prüfe deine HTML-Struktur.');
        return;
    }

    console.log('Events container found. Current children:', eventsContainer.children.length);
    console.log('Container HTML before:', eventsContainer.innerHTML);

    // Event Card erstellen
    const eventCard = document.createElement('div');
    eventCard.className = `event-card event-${type}`;

    const icon = type === 'payment' ? '💰' : '📦';

    eventCard.innerHTML = `
        <div class="event-header">
            <span class="event-title">${icon} ${data.title}</span>
        </div>
        <span class="elapsed">+${data.elapsed}ms</span>
    `;

    /*eventCard.innerHTML = `
        <div class="event-header">
            <span class="event-title">${icon} ${data.title}</span>
            <span class="elapsed">+${data.elapsed}ms</span>
        </div>
        <div class="event-body">
            <p class="event-message">✓ ${data.message}</p>
            ${data.details.map(detail => `<p class="event-detail">${detail}</p>`).join('')}
        </div>
    `;*/

    // WICHTIG: appendChild fügt UNTERHALB hinzu, überschreibt nicht!
    eventsContainer.appendChild(eventCard);

    console.log('✅ Event added! Total events now:', eventsContainer.children.length);
    console.log('Container HTML after:', eventsContainer.innerHTML);

    // Smooth entrance animation
    eventCard.style.opacity = '0';
    eventCard.style.transform = 'translateY(-20px)';

    // Animation starten
    requestAnimationFrame(() => {
        eventCard.style.transition = 'all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1)';
        eventCard.style.opacity = '1';
        eventCard.style.transform = 'translateY(0)';
    });

    // Scroll to bottom (neuestes Event sichtbar machen)
    setTimeout(() => {
        eventsContainer.scrollTop = eventsContainer.scrollHeight;
    }, 100);
}

/**
 * Prüft ob alle Events empfangen wurden
 */
function checkCompletion(receivedEvents) {
    console.log('=== CHECK COMPLETION ===');
    console.log('Received events:', receivedEvents);

    if (receivedEvents.payment && receivedEvents.inventory) {
        console.log('✅ All events received - loan processing complete');

        // Kleine Verzögerung für bessere UX
        setTimeout(() => {
            showFinalStatus({
                message: 'Loan Request vollständig verarbeitet',
                elapsed: null
            });
        }, 300);
    } else {
        console.log('⏳ Still waiting for events...');
    }
}

/**
 * Zeigt den Final Status
 */
function showFinalStatus(data) {
    console.log('=== SHOW FINAL STATUS ===');

    const finalStatus = document.getElementById('finalStatus');
    const content = finalStatus.querySelector('.response-content');

    if (!finalStatus || !content) {
        console.error('Final status containers not found!');
        return;
    }

    content.innerHTML = `
        <p class="final-message">✅ ${data.message}</p>
        <p class="final-info">Alle Services haben erfolgreich reagiert (2/2)</p>
        ${data.elapsed ? `<p class="final-time">Gesamtzeit: ${data.elapsed}ms</p>` : ''}
    `;

    finalStatus.classList.remove('hidden');

    // Smooth fade-in
    setTimeout(() => finalStatus.classList.add('visible'), 10);

    // Schließe SSE-Verbindung
    if (eventSource) {
        eventSource.close();
        eventSource = null;
    }
}

/**
 * Zeigt eine Fehlermeldung
 */
function showError(message) {
    console.log('=== SHOW ERROR ===', message);

    const eventsContainer = document.getElementById('events');

    if (!eventsContainer) {
        console.error('Events container not found for error display');
        alert('FEHLER: ' + message);
        return;
    }

    const errorCard = document.createElement('div');
    errorCard.className = 'event-card event-error';
    errorCard.innerHTML = `
        <div class="event-header">
            <span class="event-title">❌ Fehler</span>
        </div>
        <div class="event-body">
            <p class="event-message">${message}</p>
        </div>
    `;

    eventsContainer.appendChild(errorCard);
}

/**
 * Setzt die UI zurück
 */
function resetUI() {
    console.log('=== RESET UI ===');

    // Schließe offene SSE-Verbindungen
    if (eventSource) {
        console.log('Closing EventSource');
        eventSource.close();
        eventSource = null;
    }

    // Alle Container holen
    const initialResponse = document.getElementById('initialResponse');
    const timeline = document.getElementById('timeline');
    const finalStatus = document.getElementById('finalStatus');
    const events = document.getElementById('events');

    // SOFORT verstecken und leeren (keine Animation)
    if (initialResponse) {
        initialResponse.classList.remove('visible');
        initialResponse.classList.add('hidden');
    }

    if (timeline) {
        timeline.classList.remove('visible');
        timeline.classList.add('hidden');
    }

    if (finalStatus) {
        finalStatus.classList.remove('visible');
        finalStatus.classList.add('hidden');
    }

    // Events Container SOFORT leeren
    if (events) {
        console.log('Clearing events container (immediate)');
        events.innerHTML = '';
    }

    currentLoanId = null;
}

// =============================================================================
// Helper Functions
// =============================================================================

/**
 * Formatiert Währungsbeträge
 */
function formatCurrency(amount) {
    if (amount === null || amount === undefined) {
        return 'N/A';
    }
    return new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR'
    }).format(amount);
}

// =============================================================================
// Page Load - Initialization
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('=== DEMO UI LOADED ===');

    // Prüfe HTML-Struktur
    console.log('Checking HTML structure...');
    const button = document.getElementById('createLoanBtn');
    const initialResponse = document.getElementById('initialResponse');
    const timeline = document.getElementById('timeline');
    const events = document.getElementById('events');
    const finalStatus = document.getElementById('finalStatus');

    console.log('Button found:', !!button);
    console.log('Initial Response found:', !!initialResponse);
    console.log('Timeline found:', !!timeline);
    console.log('Events container found:', !!events);
    console.log('Final Status found:', !!finalStatus);

    if (!button || !initialResponse || !timeline || !events || !finalStatus) {
        console.error('❌ MISSING HTML ELEMENTS! Check your demo.html structure!');
        alert('FEHLER: HTML-Struktur unvollständig! Prüfe die Console.');
    }

    // Prüfe ob Browser SSE unterstützt
    if (typeof EventSource === 'undefined') {
        alert('Ihr Browser unterstützt keine Server-Sent Events. Bitte verwenden Sie einen modernen Browser.');
    } else {
        console.log('✅ EventSource supported');
    }

    // Cleanup bei Page Unload
    window.addEventListener('beforeunload', () => {
        if (eventSource) {
            eventSource.close();
        }
    });
});

// =============================================================================
// Debug Helper
// =============================================================================

window.debugSSE = function () {
    console.log('=== DEBUG INFO ===');
    console.log('Current Loan ID:', currentLoanId);
    console.log('EventSource status:', eventSource ? eventSource.readyState : 'null');
    console.log('EventSource states: CONNECTING=0, OPEN=1, CLOSED=2');
    console.log('Events container children:', document.getElementById('events')?.children.length);
};

window.testEvent = function () {
    console.log('=== TEST EVENT ===');
    displayEvent('payment', {
        title: 'TEST Payment Service',
        message: 'Test-Nachricht',
        details: ['Detail 1', 'Detail 2'],
        elapsed: 123
    });
};