package de.demo.lending.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Konfiguration für asynchrone Event-Verarbeitung (Alternative zu Kafka).
 * 
 * Aktiviert durch Profile "async".
 */
@Configuration
@EnableAsync
@Profile("async")
public class AsyncEventConfig {

    /**
     * Thread-Pool für asynchrone Event-Verarbeitung.
     * 
     * Dimensionierung für Performance-Tests:
     * - Core Pool Size: 10 (ausreichend für lokale Tests)
     * - Max Pool Size: 50 (kann bei Bedarf erweitert werden)
     * - Queue Capacity: 100 (Puffer für Lastspitzen)
     */
    @Bean(name = "eventTaskExecutor")
    public Executor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-event-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
