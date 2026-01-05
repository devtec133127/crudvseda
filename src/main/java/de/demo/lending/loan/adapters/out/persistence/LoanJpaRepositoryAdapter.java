package de.demo.lending.loan.adapters.out.persistence;

import java.util.Optional;

import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.application.LoanRepository;
import de.demo.lending.loan.domain.Loan;
import de.demo.lending.loan.domain.LoanId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoanJpaRepositoryAdapter implements LoanRepository {

    private final SpringLoanJpaRepository jpa;

    public LoanJpaRepositoryAdapter(SpringLoanJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Loan loan) {
        jpa.save(toEntity(loan));
    }

    @Override
    public Loan findByLoanId(LoanId loanId) {
        Optional<LoanEntity> entityList = jpa.findByLoanId(loanId.value());
        if (entityList.isEmpty()) {
            return null;
        }

        LoanEntity entity = entityList.get();
        return toDomain(entity);
    }

    private LoanEntity toEntity(Loan l) {
        log.debug("toEntity(loan={})", l);
        var e = new LoanEntity();
        e.setId(l.getLoanId().value());
        e.setUserId(l.getUserId().value());
        e.setIsbn(l.getIsbn().value());
        if (l.getCopyId() != null) {
            e.setCopyId(l.getCopyId().value());
        }
        e.setStatus(l.getStatus().name());
        e.setDueDate(l.getDueDate());
        e.setCreatedAt(l.getCreatedAt());
        e.setUpdatedAt(l.getUpdatedAt());
        e.setLoanId(l.getLoanId().value());
        return e;
    }

    private Loan toDomain(LoanEntity e) {
        log.debug("toDomain(loan={})", e);
        // Re-Konstruktor: über Factory-Methode oder Package-private ctor
        return Loan.restore(LoanId.of(e.getLoanId()), UserId.of(e.getUserId()), Isbn.of(e.getIsbn()),
                Loan.Status.valueOf(e.getStatus()), e.getDueDate(), e.getCreatedAt(), e.getUpdatedAt());
    }
}