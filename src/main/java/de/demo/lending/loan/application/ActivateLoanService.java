package de.demo.lending.loan.application;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.loan.application.dto.LoanActivatedPayload;
import de.demo.lending.loan.application.dto.event.LoanActivatedEventMapper;
import de.demo.lending.loan.domain.Loan;
import de.demo.lending.loan.domain.event.LoanActivated;
import de.demo.lending.loan.domain.port.in.ActivateLoanUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static de.demo.lending.common.events.Topics.LOAN_ACTIVATED_V1;

@Slf4j
@Service
public class ActivateLoanService implements ActivateLoanUseCase {

    private final LoanRepository loanRepository;
    private final EventPublisher publisher;
    private final DemoEventSSEPublisher uiPublisher;

    public ActivateLoanService(LoanRepository loanRepository, EventPublisher publisher, DemoEventSSEPublisher uiPublisher) {
        this.loanRepository = loanRepository;
        this.publisher = publisher;
        this.uiPublisher = uiPublisher;
    }

    @Override
    public void activate(ActivateLoanCommand command) {
        Loan foundLoan = loanRepository.findByLoanId(command.getLoanId());
        if (foundLoan == null) {
            throw new RuntimeException("Loan id not found");
        }

        foundLoan.activate(command.getCopyId());
        loanRepository.save(foundLoan);

        foundLoan.pullProducedEvents().forEach(event -> {
            if (event instanceof LoanActivated) {
                LoanActivated loanActivatedEvent = (LoanActivated) event;
                LoanActivatedPayload payload = LoanActivatedEventMapper.toPayload(loanActivatedEvent, "", "");
                log.info("Publishing event to topic {}: {}", LOAN_ACTIVATED_V1, payload);
                publisher.enqueue(LOAN_ACTIVATED_V1, payload);
                String dueDate = loanActivatedEvent.getDueDate() == null ? "" : loanActivatedEvent.getDueDate().toString();
                uiPublisher.publishLoanActivatedToUI(event.getLoanId().value(), loanActivatedEvent.getCopyId().value(), dueDate);
            }
        });
    }
}
