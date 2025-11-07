package de.demo.lending.common.adapters.out.persistence;

import java.time.Instant;

import de.demo.lending.common.config.SpringContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ProcessedEventUtil {

    public static boolean checkEvent(Class<?> clazz, String eventId) {
        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        // Wichtig ist hierbei die eventId und der consumer (hier der Klassenname), um die Eindeutig des Events und die Verarbeitung des Consumers zu garantieren
        ProcessedEventRepository repo = SpringContext.getBean(ProcessedEventRepository.class);
        String consumer = clazz.getCanonicalName();
        if (eventId != null && repo.existsByEventIdAndConsumer(eventId, consumer)) {
            // already processed -> idempotent
            log.info("Skipping already processed event {} for consumer {}", eventId, consumer);
            return true;
        }

        return false;
    }

    public static void saveEvent(Class<?> clazz, String eventId) {
        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        // Wichtig ist, dass die Schreiboperation in derselben Transaktion erfolgt wie die DB-Änderungen für die Geschäftslogik!
        ProcessedEventRepository repo = SpringContext.getBean(ProcessedEventRepository.class);
        String consumer = clazz.getCanonicalName();
        if (eventId != null) {
            repo.save(new ProcessedEventEntity(
                    eventId, consumer, Instant.now()
            ));
        }
    }
}
