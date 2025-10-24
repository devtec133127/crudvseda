package de.demo.lending.payment.application;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.payment.application.dto.PaymentCreatedPayload;
import de.demo.lending.payment.application.dto.PaymentFailedPayload;
import de.demo.lending.payment.application.dto.event.PaymentEventMapper;
import de.demo.lending.payment.domain.Money;
import de.demo.lending.payment.domain.Payment;
import de.demo.lending.payment.domain.PaymentMethod;
import de.demo.lending.payment.domain.event.PaymentCreated;
import de.demo.lending.payment.domain.event.PaymentFailed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static de.demo.lending.common.events.Topics.PAYMENT_V1;

@Slf4j
@Component
public class ExecutePayment {
    private final JsonPlaceHolderClient externalClient;
    private final PaymentRepository paymentRepository;
    private final EventPublisher publisher; // eigenes Port-Interface, s.u.

    public ExecutePayment(JsonPlaceHolderClient externalClient, PaymentRepository paymentRepository, EventPublisher publisher) {
        this.externalClient = externalClient;
        this.paymentRepository = paymentRepository;
        this.publisher = publisher;
    }

    @Transactional
    public void handle(UserId userId, LoanId loanId, BookId bookId, Money amount, PaymentMethod method, String correlationId, String causationId) {

        //log.info("Buch {} vorhanden", bookInfo.getSecond());

        // 1. Aggregate erzeugen
        Payment payment = Payment.createNew(UUID.randomUUID().toString(), loanId, bookId, userId, amount, method, correlationId);
        paymentRepository.save(payment);
        payment.pullProducedEvents().forEach(event -> {
            if (event instanceof PaymentCreated) {
                PaymentCreatedPayload payload = PaymentEventMapper.toPayload((PaymentCreated) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", PAYMENT_V1, payload);
                publisher.enqueue(PAYMENT_V1, payload);
            }
        });

        // 1. external API call
        boolean success = externalClient.call(userId.toString(), amount);
        if (!success) {
            payment.fail("Not enough money to pay!!!");
            paymentRepository.save(payment);
            payment.pullProducedEvents().forEach(event -> {
                if (event instanceof PaymentFailed) {
                    PaymentFailedPayload payload = PaymentEventMapper.toPayload((PaymentFailed) event, correlationId, causationId);
                    log.info("Publishing event to topic {}: {}", PAYMENT_V1, payload);
                    publisher.enqueue(PAYMENT_V1, payload);
                }
            });
        } else {
            // TODO payment erfolgreich
        }
    }
}
