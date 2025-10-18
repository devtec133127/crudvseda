package de.demo.lending.service;

import de.demo.lending.domain.Book;
import de.demo.lending.domain.Payment;
import de.demo.lending.dto.PaymentResponse;
import de.demo.lending.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl = "https://jsonplaceholder.typicode.com/posts";

    @Autowired
    private PaymentRepository paymentRepository;

    public PaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Verarbeite Zahlung für Ausleihe (Mock)
    public PaymentResponse processLoanPayment(UUID userId, UUID loanId, String bookTitle, double amount) {
        try {
            // Mappe Request zu JSONPlaceholder-kompatiblem Body
            Map<String, Object> body = new HashMap<>();
            body.put("title", "Payment for book with title " + bookTitle);
            body.put("body", "Amount: " + amount);
            body.put("userId", userId); // Simuliertes User-ID

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.info("Synchroner REST-Call zu Payment-Service | Endpoint: {} mit Buchtitel {} und Betrag {} ",
                    apiBaseUrl, bookTitle, amount);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiBaseUrl, entity, Map.class);
            log.info("Payment-Service antwortete: Status {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object idObj = response.getBody().get("id");
                if (idObj instanceof Number) {
                    long id = ((Number) idObj).longValue();

                    Payment payment = new Payment();
                    payment.setLoanId(loanId);
                    payment.setUserId(userId);
                    payment.setAmount(amount);
                    payment.setStatus("bezahlt");

                    try {
                        paymentRepository.save(payment);
                        log.debug("Saved Payment entity for loanId={} userId={} amount={}", loanId, userId, amount);
                    } catch (Exception dbEx) {
                        log.error("Fehler beim Speichern des Payments", dbEx);
                        // Entscheidung: trotzdem SUCCESS weil remote payment bestätigt? Oder FAILED? Hier controlliert ableiten.
                    }

                    log.info("Zahlung genehmigt | Loan-ID: {} | User-ID {}", loanId, userId);
                    return new PaymentResponse(id, bookTitle, amount, "SUCCESS");
                } else {
                    log.warn("Payment API returned non-numeric id: {}", idObj);
                    return new PaymentResponse(-1, bookTitle, amount, "FAILED");
                }
            } else {
                log.warn("Payment API not successful: status={} body={}", response.getStatusCode(), response.getBody());
                return new PaymentResponse(-1, bookTitle, amount, "FAILED");
            }
        } catch (Exception e) {
            // Fallback für Fehlerszenarien (z. B. Netzwerkfehler)
            return new PaymentResponse(-1, bookTitle, amount, "FAILED");
        }
    }

    // Verarbeite Zahlung für Verlängerung (z. B. Gebühr)
    public PaymentResponse processExtensionPayment(UUID userId, UUID loanId,String bookTitle, double fee) {
        return processLoanPayment(userId, loanId, bookTitle, fee); // Wiederverwendung
    }

    // Rückerstattung bei Rückgabe
    public void refundPayment(UUID userId) {
        System.out.println("Rückerstattung für User " + userId + " ausgeführt.");
    }
}