package de.demo.lending.payment.application;

import de.demo.lending.payment.domain.Payment;

public interface PaymentRepository {
    /**
     * Persistiert Aktualisierungen eines Payment (z.B. Statuswechsel).
     */
    Payment save(Payment payment);
}
