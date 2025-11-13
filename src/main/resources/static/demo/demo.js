// =============================================================================
// Demo UI für Event-Driven Loan System
// Empfängt Server-Sent Events und zeigt Event-Timeline in Echtzeit
// =============================================================================
const API_BASE = 'http://localhost:8080/api/demo';
let eventSource = null;

// =============================================================================
// Button Click Handler - Startet den Loan Request
// =============================================================================
document.getElementById('createLoanBtn').addEventListener('click', async () => {
    console.log('Button clicked - creating loan request...');

    // Reset UI
    resetUI();

    // Button deaktivieren während Request läuft
        const button = document.getElementById('createLoanBtn');
        button.disabled = true;
        button.textContent = '⏳ Erstelle Loan Request...';

        try {
            // POST Request zum Backend
            const response = await fetch(`${API_BASE}/loans`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: '518aeace-387a-4a16-a0b8-b6d6fa9e8bc3',
                    bookTitle: 'C#'
                    //amount: 5000,
                    //customerId: 'CUST-' + Math.floor(Math.random() * 10000),
                    //purpose: 'Fahrzeugkauf'
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const loan = await response.json();
            console.log('Loan created:', loan);

            currentLoanId = loan.id;

            // Zeige Initial Response
            displayInitialResponse(loan);

            // Öffne SSE Stream für Live-Updates
            subscribeToEvents(loan.id);

        } catch (error) {
            console.error('Error creating loan:', error);
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
    console.log('Opening SSE stream for loan:', loanId);

    // Schließe vorherige Verbindung falls vorhanden
    if (eventSource) {
        eventSource.close();
    }

    // Öffne neue SSE-Verbindung
    eventSource = new EventSource(`${API_BASE}/loans/${loanId}/events`);

    // Tracking für empfangene Events
    const receivedEvents = {
        payment: false,
        inventory: false
    };
    const startTime = Date.now();

    // Event-Listener für verschiedene Event-Typen

    // 1. Loan Created (optional)
    eventSource.addEventListener('loan-created', (e) => {
        console.log('SSE Event: loan-created', e.data);
        const data = JSON.parse(e.data);
        // Optional: Zeige Initialisierungs-Nachricht
    });

    // 2. Payment Completed
    eventSource.addEventListener('payment-completed', (e) => {
        console.log('SSE Event: payment-completed', e.data);
        const data = JSON.parse(e.data);
        const elapsed = Date.now() - startTime;

        displayEvent('payment', {
            title: 'Payment Service',
            message: data.message,
            details: [
                `Transaction-ID: ${data.transactionId}`,
                `Betrag: ${formatCurrency(data.amount)}`
            ],
            elapsed: data.elapsedMs || elapsed
        });

        receivedEvents.payment = true;
        checkCompletion(receivedEvents);
    });

    // 3. Inventory Reserved
    eventSource.addEventListener('inventory-reserved', (e) => {
        console.log('SSE Event: inventory-reserved', e.data);
        const data = JSON.parse(e.data);
        const elapsed = Date.now() - startTime;

        displayEvent('inventory', {
            title: 'Inventory Service',
            message: data.message,
            details: [
                `Reservation-ID: ${data.reservationId}`,
                `Artikel: ${data.article || 'Fahrzeug'}`
            ],
            elapsed: data.elapsedMs || elapsed
        });

        receivedEvents.inventory = true;
        checkCompletion(receivedEvents);
    });

    // 4. Loan Finalized (optional)
    eventSource.addEventListener('loan-finalized', (e) => {
        console.log('SSE Event: loan-finalized', e.data);
        const data = JSON.parse(e.data);

        showFinalStatus({
            message: data.message,
            elapsed: data.elapsedMs
        });

        // Schließe SSE-Verbindung
        eventSource.close();
        eventSource = null;
    });

    // Error Handling
    eventSource.onerror = (error) => {
        console.error('SSE connection error:', error);

        if (eventSource.readyState === EventSource.CLOSED) {
            console.log('SSE connection closed');
            showError('Verbindung zum Server verloren');
        } else if (eventSource.readyState === EventSource.CONNECTING) {
            console.log('SSE reconnecting...');
        }
    };

    // Connection opened
    eventSource.onopen = () => {
        console.log('SSE connection opened');
    };
}

// =============================================================================
// UI Display Functions
// =============================================================================

/**
 * Zeigt die initiale Response vom Loan Service
 */
function displayInitialResponse(loan) {
    const container = document.getElementById('initialResponse');
    const content = container.querySelector('.response-content');

    const timestamp = new Date().toLocaleTimeString('de-DE', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        fractionalSecondDigits: 3
    });

    content.innerHTML = `
        <p><strong>Status:</strong> <span class="status-badge status-pending">${loan.status}</span></p>
        <p><strong>Loan-ID:</strong> <code>${loan.id}</code></p>
        <p><strong>Betrag:</strong> ${formatCurrency(loan.amount)}</p>
        <p><strong>⏱️ Empfangen:</strong> ${timestamp}</p>
    `;

    container.classList.remove('hidden');
    document.getElementById('timeline').classList.remove('hidden');

    // Smooth fade-in
    setTimeout(() => container.classList.add('visible'), 10);
}

/**
 * Zeigt ein Event in der Timeline
 */
function displayEvent(type, data) {
    const eventsContainer = document.getElementById('events');

    // Event Card erstellen
    const eventCard = document.createElement('div');
    eventCard.className = `event-card event-${type}`;

    const icon = type === 'payment' ? '💰' : '📦';

    eventCard.innerHTML = `
        <div class="event-header">
            <span class="event-title">${icon} ${data.title}</span>
            <span class="elapsed">+${data.elapsed}ms</span>
        </div>
        <div class="event-body">
            <p class="event-message">✓ ${data.message}</p>
            ${data.details.map(detail => `<p class="event-detail">${detail}</p>`).join('')}
        </div>
    `;

    eventsContainer.appendChild(eventCard);

    // Smooth entrance animation
    eventCard.style.opacity = '0';
    eventCard.style.transform = 'translateY(-20px)';

    requestAnimationFrame(() => {
        eventCard.style.transition = 'all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1)';
        eventCard.style.opacity = '1';
        eventCard.style.transform = 'translateY(0)';
    });

    // Scroll to bottom (latest event)
    eventsContainer.scrollTop = eventsContainer.scrollHeight;
}

/**
 * Prüft ob alle Events empfangen wurden
 */
function checkCompletion(receivedEvents) {
    if (receivedEvents.payment && receivedEvents.inventory) {
        console.log('All events received - loan processing complete');

        // Kleine Verzögerung für bessere UX
        setTimeout(() => {
            showFinalStatus({
                message: 'Loan Request vollständig verarbeitet',
                elapsed: null
            });
        }, 300);
    }
}

/**
 * Zeigt den Final Status
 */
function showFinalStatus(data) {
    const finalStatus = document.getElementById('finalStatus');
    const content = finalStatus.querySelector('.response-content');

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
    const eventsContainer = document.getElementById('events');

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
    console.log('Resetting UI...');

    // Schließe offene SSE-Verbindungen
    if (eventSource) {
        eventSource.close();
        eventSource = null;
    }

    // Verstecke alle Sections
    document.getElementById('initialResponse').classList.remove('visible');
    document.getElementById('timeline').classList.remove('visible');
    document.getElementById('finalStatus').classList.remove('visible');

    // Leere Event-Container nach Animation
    setTimeout(() => {
        document.getElementById('initialResponse').classList.add('hidden');
        document.getElementById('timeline').classList.add('hidden');
        document.getElementById('finalStatus').classList.add('hidden');
        document.getElementById('events').innerHTML = '';
    }, 300);

    currentLoanId = null;
}

// =============================================================================
// Helper Functions
// =============================================================================

/**
 * Formatiert Währungsbeträge
 */
function formatCurrency(amount) {
    return new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR'
    }).format(amount);
}

/**
 * Formatiert Zeitstempel
 */
function formatTimestamp(timestamp) {
    return new Date(timestamp).toLocaleTimeString('de-DE', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        fractionalSecondDigits: 3
    });
}

// =============================================================================
// Page Load - Initialization
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('Demo UI loaded');

    // Prüfe ob Browser SSE unterstützt
    if (typeof EventSource === 'undefined') {
        alert('Ihr Browser unterstützt keine Server-Sent Events. Bitte verwenden Sie einen modernen Browser.');
    }

    // Cleanup bei Page Unload
    window.addEventListener('beforeunload', () => {
        if (eventSource) {
            eventSource.close();
        }
    });
});

// =============================================================================
// Debug Helper (nur für Entwicklung)
// =============================================================================

// Globale Funktion für Console-Tests
window.debugSSE = function() {
    console.log('Current Loan ID:', currentLoanId);
    console.log('EventSource status:', eventSource ? eventSource.readyState : 'null');
    console.log('EventSource states: CONNECTING=0, OPEN=1, CLOSED=2');
};