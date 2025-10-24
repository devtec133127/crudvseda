package de.demo.lending.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Konfiguration für asynchrone Event-Verarbeitung (Alternative zu Kafka).
 * <p>
 * Aktiviert durch Profile "async".
 */
@Configuration
@EnableAsync
public class AsyncEventConfig {
}
