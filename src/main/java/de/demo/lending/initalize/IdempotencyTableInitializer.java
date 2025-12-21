package de.demo.lending.initalize;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdempotencyTableInitializer {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyTableInitializer.class);

    @Bean
    public boolean initializeIdempotencyTable(DataSource dataSource) {
        String sql = """
                CREATE TABLE IF NOT EXISTS idempotent_action (
                    key TEXT NOT NULL,
                    type TEXT NOT NULL,
                    client TEXT NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    last_run_at TIMESTAMP WITH TIME ZONE,
                    completed_at TIMESTAMP WITH TIME ZONE,
                    result TEXT,
                    result_type TEXT,
                    -- WICHTIG: Die Reihenfolge im Constraint muss (key, type, client) sein,
                    -- weil die Library es im SQL genau so vorgibt!
                    CONSTRAINT uk_idempotent_action UNIQUE (key, type, client)
                );
                
                -- Optionaler Index für schnellere Lookups
                CREATE INDEX IF NOT EXISTS idx_idempotent_action_lookup 
                ON idempotent_action (key, type, client);
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