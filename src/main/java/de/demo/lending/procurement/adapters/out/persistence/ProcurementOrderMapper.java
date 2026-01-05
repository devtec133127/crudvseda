package de.demo.lending.procurement.adapters.out.persistence;

import java.util.UUID;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.Isbn;
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
        //entity.setBookTitle(domain.getBookTitle().toString());
        if (domain.getExternalLibraryId() != null) {
            entity.setExternalLibraryId(domain.getExternalLibraryId().toString());
        }
        entity.setBookId(domain.getBookId() != null ? domain.getBookId().value() : null);
        entity.setIsbn(domain.getIsbn() != null ? domain.getIsbn().value() : null);
        entity.setStatus(domain.getStatus());
        entity.setOrderedAt(domain.getOrderedAt());
        entity.setEstimatedArrival(domain.getEstimatedArrival());
        entity.setReceivedAt(domain.getReceivedAt());

        return entity;
    }

    /**
     * JPA Entity → Domain Model
     */
    public ProcurementOrder toDomain(ProcurementOrderEntity entity) {
        ExternalLibraryId libraryId = null;
        if (entity.getExternalLibraryId() != null) {
            libraryId = ExternalLibraryId.of(UUID.fromString(entity.getExternalLibraryId()));
        }
        return ProcurementOrder.create(
                ProcurementOrderId.of(entity.getId()),
                LoanId.of(entity.getLoanId()),
                libraryId,
                BookId.of(entity.getBookId()),
                Isbn.of(entity.getIsbn()),
                entity.getStatus(),
                entity.getOrderedAt(),
                entity.getEstimatedArrival(),
                entity.getReceivedAt(),
                entity.getReceivedBy()
        );
    }
}
