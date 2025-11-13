package de.demo.lending.loan.adapters.in.demo;

import de.demo.lending.loan.adapters.in.demo.dto.DemoEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-Memory Event Store für Demo-Zwecke.
 * Sammelt Events aus Payment/Inventory Services und
 * streamt sie zur UI.
 * <p>
 * NICHT für Production! Keine Persistierung, kein Clustering.
 */
@Component
public class DemoEventStore {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Map<String, List<DemoEvent>> eventHistory = new ConcurrentHashMap<>();

    /**
     * Öffnet SSE-Stream für einen Loan
     */
    public SseEmitter subscribe(String loanId) {
        SseEmitter emitter = new SseEmitter(60000L);

        // Emitter registrieren
        emitters.computeIfAbsent(loanId, k -> new CopyOnWriteArrayList<>())
                .add(emitter);

        // Bei Disconnect aufräumen
        emitter.onCompletion(() -> removeEmitter(loanId, emitter));
        emitter.onTimeout(() -> removeEmitter(loanId, emitter));

        // Historische Events sofort senden
        eventHistory.getOrDefault(loanId, List.of())
                .forEach(event -> sendEvent(emitter, event));

        return emitter;
    }

    /**
     * Publiziert Event zu allen Subscribern eines Loans
     */
    public void publishEvent(String loanId, DemoEvent event) {
        // In Historie speichern
        eventHistory.computeIfAbsent(loanId, k -> new CopyOnWriteArrayList<>())
                .add(event);

        // An alle aktiven Emitter senden
        emitters.getOrDefault(loanId, List.of())
                .forEach(emitter -> sendEvent(emitter, event));
    }

    private void sendEvent(SseEmitter emitter, DemoEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event.getType())
                    .data(event));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private void removeEmitter(String loanId, SseEmitter emitter) {
        emitters.getOrDefault(loanId, List.of()).remove(emitter);
    }
}