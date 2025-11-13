const API_BASE = 'http://localhost:8080/api';
let eventSource = null;

document.getElementById('createLoanBtn').addEventListener('click', async () => {
    // Reset UI
    resetUI();

    // POST Request
    const response = await fetch(`${API_BASE}/loans`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            amount: 5000,
            customerId: 'CUST-123',
            purpose: 'Fahrzeugkauf'
        })
    });

    const loan = await response.json();

    // Zeige Initial Response
    displayInitialResponse(loan);

    // Öffne SSE Stream
    subscribeToEvents(loan.id);
});

function subscribeToEvents(loanId) {
    eventSource = new EventSource(`${API_BASE}/loans/${loanId}/events`);

    const startTime = Date.now();
    const receivedEvents = {payment: false, inventory: false};

    eventSource.addEventListener('payment-completed', (e) => {
        const data = JSON.parse(e.data);
        const elapsed = Date.now() - startTime;
        displayEvent('payment', data, elapsed);
        receivedEvents.payment = true;
        checkCompletion(receivedEvents);
    });

    eventSource.addEventListener('inventory-reserved', (e) => {
        const data = JSON.parse(e.data);
        const elapsed = Date.now() - startTime;
        displayEvent('inventory', data, elapsed);
        receivedEvents.inventory = true;
        checkCompletion(receivedEvents);
    });

    eventSource.onerror = () => {
        console.error('SSE connection lost');
        eventSource.close();
    };
}

function displayInitialResponse(loan) {
    const container = document.getElementById('initialResponse');
    container.querySelector('.response-content').innerHTML = `
        <p><strong>Status:</strong> ${loan.status}</p>
        <p><strong>Loan-ID:</strong> ${loan.id}</p>
        <p><strong>⏱️ Empfangen:</strong> ${new Date().toLocaleTimeString('de-DE')}.${Date.now() % 1000}</p>
    `;
    container.classList.remove('hidden');
    document.getElementById('timeline').classList.remove('hidden');
}

function displayEvent(type, data, elapsed) {
    const eventsContainer = document.getElementById('events');

    const eventCard = document.createElement('div');
    eventCard.className = `event-card ${type}`;

    const icon = type === 'payment' ? '💰' : '📦';
    const title = type === 'payment' ? 'Payment Service' : 'Inventory Service';

    eventCard.innerHTML = `
        <div class="event-header">
            <span>${icon} ${title}</span>
            <span class="elapsed">+${elapsed}ms</span>
        </div>
        <div class="event-body">
            <p>✓ ${data.message}</p>
            <p><strong>ID:</strong> ${data.id}</p>
            ${data.details ? `<p>${data.details}</p>` : ''}
        </div>
    `;

    eventsContainer.appendChild(eventCard);

    // Smooth scroll animation
    eventCard.style.opacity = '0';
    eventCard.style.transform = 'translateY(-20px)';
    setTimeout(() => {
        eventCard.style.transition = 'all 0.5s ease';
        eventCard.style.opacity = '1';
        eventCard.style.transform = 'translateY(0)';
    }, 10);
}

function checkCompletion(receivedEvents) {
    if (receivedEvents.payment && receivedEvents.inventory) {
        const finalStatus = document.getElementById('finalStatus');
        finalStatus.querySelector('.response-content').innerHTML = `
            <p>✅ Loan Request vollständig verarbeitet</p>
            <p>Alle Services haben reagiert (2/2)</p>
        `;
        finalStatus.classList.remove('hidden');

        // Close SSE connection
        if (eventSource) {
            eventSource.close();
        }
    }
}

function resetUI() {
    document.getElementById('initialResponse').classList.add('hidden');
    document.getElementById('timeline').classList.add('hidden');
    document.getElementById('finalStatus').classList.add('hidden');
    document.getElementById('events').innerHTML = '';

    if (eventSource) {
        eventSource.close();
    }
}