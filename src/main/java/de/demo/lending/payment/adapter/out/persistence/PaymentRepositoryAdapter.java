package de.demo.lending.payment.adapter.out.persistence;

import java.util.UUID;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.payment.application.PaymentRepository;
import de.demo.lending.payment.domain.Money;
import de.demo.lending.payment.domain.Payment;
import de.demo.lending.payment.domain.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringPaymentRepository jpa;

    public PaymentRepositoryAdapter(SpringPaymentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Payment save(Payment payment) {
        // map Domain -> Entity und save
        PaymentEntity e = new PaymentEntity();
        e.setId(payment.getPaymentId().value());
        e.setState(payment.getStatus().name());
        e.setUpdatedAt(payment.getUpdatedAt());
        e.setCreatedAt(payment.getCreatedAt());
        e.setLoanId(payment.getLoanId().toString());
        e.setBookId(payment.getBookId().toString());
        e.setUserId(payment.getUserId().toString());
        e.setAmountCents(payment.getAmount().getCents());
        e.setCurrency(payment.getAmount().getCurrency());
        e.setState(payment.getStatus().name());
        e.setPaymentMethodJson(payment.getMethod().toString());

        PaymentEntity save = this.jpa.save(e);

        // map Entity to Domain of saved entity
        return Payment.create(
                save.getId(),
                LoanId.of(UUID.fromString(save.getLoanId())),
                BookId.of(save.getBookId()),
                UserId.of(UUID.fromString(save.getUserId())),
                new Money(save.getAmountCents(), save.getCurrency()),
                PaymentMethod.fromString(save.getPaymentMethodJson()).get()
        );
    }
}
