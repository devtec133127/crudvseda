package de.demo.lending.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transferwise.idempotence4j.core.ActionRepository;
import com.transferwise.idempotence4j.core.DefaultIdempotenceService;
import com.transferwise.idempotence4j.core.IdempotenceService;
import com.transferwise.idempotence4j.core.ResultSerializer;
import com.transferwise.idempotence4j.core.metrics.Metrics;
import com.transferwise.idempotence4j.core.metrics.MetricsPublisher;
import com.transferwise.idempotence4j.core.serializers.json.JsonResultSerializer;
import com.transferwise.idempotence4j.postgres.JdbcPostgresActionRepository;
import com.transferwise.idempotence4j.postgres.JdbcPostgresLockProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class IdempotencyConfig {

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public ActionRepository idempotencyStore() {
        // In der Postgres-Erweiterung heißt die Klasse PostgresIdempotencyStore
        return new JdbcPostgresActionRepository(template);
    }

    @Bean
    public IdempotenceService idempotenceService(
            ActionRepository store,
            PlatformTransactionManager transactionManager) {

        // 2. ResultSerializer (Nutzt Jackson für JSON-Speicherung)
        // Meist gibt es eine Klasse "JacksonResultSerializer"
        ResultSerializer resultSerializer = new JsonResultSerializer(objectMapper);

        // 3. MetricsPublisher (Dummy/No-Op Version)
        MetricsPublisher metricsPublisher = new MetricsPublisher() {
            @Override
            public void publish(Metrics metrics) {
                // nothing
            }
        };
        
        // DefaultIdempotenceService ist die gängige Implementierung
        return new DefaultIdempotenceService(
                transactionManager,
                new JdbcPostgresLockProvider(template), // Prüfe diesen Namen!
                idempotencyStore(),
                resultSerializer,
                metricsPublisher
        );
    }
}
