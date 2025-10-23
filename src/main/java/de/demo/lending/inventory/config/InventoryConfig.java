package de.demo.lending.inventory.config;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.adapters.out.outbox.messaging.async.AsyncEventBus;
import de.demo.lending.inventory.adapters.in.messaging.AsyncInventoryEventListener;
import de.demo.lending.inventory.application.InventoryRepository;
import de.demo.lending.inventory.application.OpenLibraryClient;
import de.demo.lending.inventory.application.ReservationRepository;
import de.demo.lending.inventory.application.ReserveBook;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class InventoryConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OpenLibraryClient openLibraryClient(RestTemplate restTemplate) {
        return new OpenLibraryClient(restTemplate);
    }

    @Bean
    public ReserveBook reserveBook(OpenLibraryClient client, ReservationRepository resRepo,
                                   InventoryRepository invRepo, EventPublisher publisher) {
        return new ReserveBook(client, resRepo, invRepo, publisher);
    }

    @Bean
    public AsyncInventoryEventListener asyncInventoryEventListener(InventoryRepository repo, EventPublisher events,
                                                                   ReserveBook reserveBook, AsyncEventBus eventBus) {
        return new AsyncInventoryEventListener(repo, events, reserveBook, eventBus);
    }
}
