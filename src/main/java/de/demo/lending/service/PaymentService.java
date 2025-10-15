package de.demo.lending.service;

import de.demo.lending.domain.Payment;
import de.demo.lending.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // Verarbeite Zahlung für Ausleihe (Mock)
    public boolean processLoanPayment(UUID userId, double amount) {
        // Simuliere Zahlung: Prüfe User-Kreditlimit oder DB-Check
        if (userId != null && amount > 0) {
            // Simuliere und speichere
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID());
            payment.setUserId(userId);
            payment.setAmount(amount);
            payment.setStatus("PAID");
            // Setze loan_id wenn verfügbar
            paymentRepository.save(payment);

            // In Realität: Aufruf an externe API oder DB-Speicherung
            System.out.println("Zahlung von " + amount + " für User " + userId + " verarbeitet.");
            return true; // Erfolgreich
        }
        return false; // Fehlschlag
    }

    // Verarbeite Zahlung für Verlängerung (z. B. Gebühr)
    public boolean processExtensionPayment(UUID userId, double fee) {
        return processLoanPayment(userId, fee); // Wiederverwendung
    }

    // Rückerstattung bei Rückgabe
    public void refundPayment(UUID userId) {
        System.out.println("Rückerstattung für User " + userId + " ausgeführt.");
    }
}