package de.demo.lending.common.adapters.out.outbox.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    /**
     * Unveröffentlichte Events – FIFO, begrenzt.
     */
    @Query(value = """
            SELECT * FROM outbox
            WHERE published_at IS NULL
            ORDER BY id ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<OutboxEntity> findUnpublished(@Param("limit") int limit);

    /**
     * Batch-Update: markiert Events als veröffentlicht.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
            UPDATE outbox
            SET published_at = :publishedAt,
                attempt = attempt + 1
            WHERE id IN (:ids)
            """, nativeQuery = true)
    int markPublished(@Param("ids") Collection<Long> ids,
                      @Param("publishedAt") Instant publishedAt);
}