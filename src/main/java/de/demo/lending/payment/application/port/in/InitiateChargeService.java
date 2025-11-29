package de.demo.lending.payment.application.port.in;

import static de.demo.lending.common.events.Topics.PAYMENT_INITIATED_V1;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.payment.application.PaymentRepository;
import de.demo.lending.payment.application.dto.PaymentInitiatedEventMapper;
import de.demo.lending.payment.application.dto.PaymentInitiatedPayload;
import de.demo.lending.payment.domain.Payment;
import de.demo.lending.payment.domain.event.PaymentInitiated;
import de.demo.lending.payment.domain.port.in.InitiateChargeUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InitiateChargeService implements InitiateChargeUseCase {
    private final PaymentRepository repository;
    private final EventPublisher publisher;
    private final DemoEventSSEPublisher uiPublisher;

    public InitiateChargeService(PaymentRepository repository, EventPublisher publisher, DemoEventSSEPublisher uiPublisher) {
        this.repository = repository;
        this.publisher = publisher;
        this.uiPublisher = uiPublisher;
    }


    @Override
    public Payment initiate(InitiateChargeCommand command) {

        Payment payment = Payment.initiate(command.getLoanId(), command.getUserId());
        repository.save(payment);

        payment.pullProducedEvents().forEach(event -> {
            if (event instanceof PaymentInitiated) {
                PaymentInitiated paymentInitiatedEvent = (PaymentInitiated) event;
                PaymentInitiatedPayload payload = PaymentInitiatedEventMapper.toPayload(paymentInitiatedEvent, "", "");
                log.info("Publishing event to topic {}: {}", PAYMENT_INITIATED_V1, payload);
                publisher.enqueue(PAYMENT_INITIATED_V1, payload);
                uiPublisher.publishPaymentInitiatedToUI(event.getLoanId().value(), paymentInitiatedEvent.getUserId().value());
            }
        });

        return payment;
    }
}
