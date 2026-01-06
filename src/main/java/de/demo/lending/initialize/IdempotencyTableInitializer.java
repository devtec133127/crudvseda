package de.demo.lending.initialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class IdempotencyTableInitializer {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyTableInitializer.class);

    @Bean
    public boolean initializeIdempotencyTable(DataSource dataSource) {
        String sql = """
                CREATE TABLE IF NOT EXISTS idempotent_action (
                    id BIGSERIAL PRIMARY KEY,
                    client_id TEXT NOT NULL,
                    type TEXT NOT NULL,
                    key TEXT NOT NULL,
                    status TEXT NOT NULL,
                    result TEXT,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    CONSTRAINT uk_idempotent_action UNIQUE (client_id, type, key)
                );
                CREATE INDEX IF NOT EXISTS idx_idempotent_action_lookup 
                ON idempotent_action (client_id, type, key);
                """;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            log.info("Checking/Creating idempotency table...");
            statement.execute(sql);
            return true;

        } catch (Exception e) {
            log.error("Could not create idempotency table!", e);
            // Wir werfen hier keine Exception, damit die App trotzdem startet,
            // aber du siehst den Fehler im Log.
            return false;
        }
    }
}
