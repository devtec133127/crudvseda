package de.demo.lending.procurement.adapters.out.persistence;

import de.demo.lending.procurement.domain.ProcurementOrder;
import de.demo.lending.procurement.domain.port.out.ProcurementOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProcurementOrderRepositoryAdapter implements ProcurementOrderRepository {

    private final SpringProcurementOrderRepository jpaRepo;
    private final ProcurementOrderMapper mapper;

    @Override
    public void save(ProcurementOrder order) {
        ProcurementOrderEntity jpaEntity = mapper.toEntity(order);
        jpaRepo.save(jpaEntity);
    }
}
