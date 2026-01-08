package de.demo.lending.payment.adapter.out.persistence;

import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.common.valueobjects.UserId;
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
        e.setLoanId(payment.getLoanId().value().toString());
        e.setAmountCents(payment.getAmount().getCents());
        e.setCurrency(payment.getAmount().getCurrency());
        e.setState(payment.getStatus().name());
        e.setUserId(payment.getUserId().value().toString());
        e.setPaymentMethodJson(payment.getMethod().toString());

        PaymentEntity save = this.jpa.save(e);

        // map Entity to Domain of saved entity
        return Payment.create(
                save.getId(),
                LoanId.of(save.getLoanId()),
                UserId.of(save.getUserId()),
                new Money(save.getAmountCents(), save.getCurrency()),
                PaymentMethod.fromString(save.getPaymentMethodJson()).get()
        );
    }
}
