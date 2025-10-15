package de.demo.lending.loan.adapters.out.persistence;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.application.LoanRepository;
import de.demo.lending.loan.domain.Loan;
import de.demo.lending.loan.domain.LoanId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(value="service.role", havingValue="loan")
public class LoanJpaRepositoryAdapter implements LoanRepository {

    private final SpringLoanJpaRepository jpa;

    public LoanJpaRepositoryAdapter(SpringLoanJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void save(Loan loan) {
        jpa.save(toEntity(loan));
    }

    @Override
    public Optional<Loan> findById(LoanId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    private LoanEntity toEntity(Loan l) {
        log.debug("toEntity(loan={})", l);
        var e = new LoanEntity();
        e.setId(l.getId().value());
        e.setUserId(l.getUserId().value());
        e.setBookId(l.getBookId().value());
        if(l.getCopyId() != null) {
            e.setCopyId(l.getCopyId().value());
        }
        e.setStatus(l.getStatus().name());
        e.setDueDate(l.getDueDate());
        e.setCreatedAt(l.getCreatedAt());
        e.setUpdatedAt(l.getUpdatedAt());
        return e;
    }

    private Loan toDomain(LoanEntity e) {
        log.debug("toDomain(loan={})", e);
        // Re-Konstruktor: über Factory-Methode oder Package-private ctor
        return Loan.restore(LoanId.of(e.getId()), UserId.of(e.getUserId()), BookId.of(e.getBookId()),
                CopyId.of(e.getCopyId()), Loan.Status.valueOf(e.getStatus()), e.getDueDate(), e.getCreatedAt(), e.getUpdatedAt());
    }
}