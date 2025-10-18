package de.demo.lending.inventory.adapters.out.persistence;


import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.inventory.application.InventoryRepository;
import de.demo.lending.inventory.domain.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
//@ConditionalOnProperty(value = "service.role", havingValue = "inventory")
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final SpringInventoryCopyRepository jpa;

    public InventoryRepositoryAdapter(SpringInventoryCopyRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Optional<CopyId> reserveFirstAvailable(BookId bookId) {
        // 1) Suche verfügbare Kopien
        List<InventoryCopyEntity> avail = jpa.findAvailableByBookId(bookId.value());
        if (avail.isEmpty()) return Optional.empty();

        // 2) Nimm die erste, ändere Zustand und speichere (in einer TX)
        InventoryCopyEntity e = avail.get(0);
        e.setState("RESERVED");
        e.setUpdatedAt(Instant.now());
        jpa.save(e);

        return Optional.of(CopyId.of(e.getId()));
    }

    @Override
    public Optional<InventoryCopy> findById(CopyId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    @Transactional
    public void save(InventoryCopy copy) {
        // map Domain -> Entity und save
        InventoryCopyEntity e = toEntity(copy);
        jpa.save(e);
    }

    // -------------------------
    // Mapper Domain <-> Entity
    // -------------------------
    private InventoryCopy toDomain(InventoryCopyEntity e) {
        return new InventoryCopy(
                CopyId.of(e.getId()),
                BookId.of(e.getBookId()),
                InventoryCopy.State.valueOf(e.getState()),
                e.getCreatedAt() != null ? e.getCreatedAt() : Instant.now(),
                e.getUpdatedAt() != null ? e.getUpdatedAt() : Instant.now(),
                e.getLocation()
        );
    }

    private InventoryCopyEntity toEntity(InventoryCopy d) {
        return InventoryCopyEntity.builder()
                .id(d.getId().value())
                .bookId(d.getBookId().value())
                .state(d.getState().name())
                .location(d.getLocation())
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt() : Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}