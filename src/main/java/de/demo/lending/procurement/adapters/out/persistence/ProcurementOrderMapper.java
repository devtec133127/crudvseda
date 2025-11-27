package de.demo.lending.procurement.adapters.out.persistence;

import java.util.UUID;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.ExternalLibraryId;
import de.demo.lending.procurement.domain.ProcurementOrder;
import de.demo.lending.procurement.domain.ProcurementOrderId;
import org.springframework.stereotype.Component;

@Component
public class ProcurementOrderMapper {
    /**
     * Domain Model → JPA Entity
     */
    public ProcurementOrderEntity toEntity(ProcurementOrder domain) {
        ProcurementOrderEntity entity = new ProcurementOrderEntity();

        entity.setId(domain.getId());
        entity.setLoanId(domain.getLoanId().value());
        entity.setBookTitle(domain.getBookTitle().toString());
        if (domain.getExternalLibraryId() != null) {
            entity.setExternalLibraryId(domain.getExternalLibraryId().toString());
        }
        entity.setExternalOrderId(domain.getExternalOrderId());
        entity.setIsbn(domain.getIsbn() != null ? domain.getIsbn().value() : null);
        entity.setStatus(domain.getStatus());
        entity.setOrderedAt(domain.getOrderedAt());
        entity.setEstimatedArrival(domain.getEstimatedArrival());
        entity.setReceivedAt(domain.getReceivedAt());
        entity.setReceivedBy(domain.getReceivedBy());

        return entity;
    }

    /**
     * JPA Entity → Domain Model
     */
    public ProcurementOrder toDomain(ProcurementOrderEntity entity) {
        return ProcurementOrder.create(
                ProcurementOrderId.of(entity.getId()),
                LoanId.of(entity.getLoanId()),
                BookTitle.of(entity.getBookTitle()),
                entity.getExternalLibraryId() != null ? ExternalLibraryId.of(UUID.fromString(entity.getExternalLibraryId())) : null,
                entity.getExternalOrderId(),
                entity.getIsbn() != null ? BookId.of(entity.getIsbn()) : null,
                entity.getStatus(),
                entity.getOrderedAt(),
                entity.getEstimatedArrival(),
                entity.getReceivedAt(),
                entity.getReceivedBy()
        );
    }
}
