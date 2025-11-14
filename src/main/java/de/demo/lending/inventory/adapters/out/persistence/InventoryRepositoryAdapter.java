package de.demo.lending.inventory.adapters.out.persistence;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.InventoryRepository;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.inventory.domain.ReservationId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
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
                e.getId().toString(),
                null,
                //CopyId.of(e.getId()),
                BookId.of(e.getBookId()),
                UserId.of(UUID.fromString(e.getUserId())),
                e.getBookTitle(),
                //e.getCreatedAt() != null ? e.getCreatedAt() : Instant.now(),
                e.getUpdatedAt() != null ? e.getUpdatedAt() : Instant.now(),
                ReservationId.of(UUID.fromString(e.getReservationId()))
        );
    }

    private InventoryCopyEntity toEntity(InventoryCopy d) {
        return InventoryCopyEntity.builder()
                .id(UUID.fromString(d.getId()))
                .bookId(d.getBookId().value())
                .bookTitle(d.getBookTitle())
                .userId(d.getUserId().toString())
                .state(d.getState().name())
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt() : Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}