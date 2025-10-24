package de.demo.lending.payment.application;

import de.demo.lending.payment.domain.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JsonPlaceHolderClient {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl = "https://jsonplaceholder.typicode.com/posts";

    public JsonPlaceHolderClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean call(String userId, Money amount) {
        try {
            // Mappe Request zu JSONPlaceholder-kompatiblem Body
            Map<String, Object> body = new HashMap<>();
            body.put("body", "Amount: " + amount);
            body.put("userId", userId); // Simuliertes User-ID

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.info("Synchroner REST-Call zu Payment-Service | Endpoint: {} mit userId {} und Betrag {} ",
                    apiBaseUrl, userId, amount);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiBaseUrl, entity, Map.class);
            log.info("Payment-Service antwortete: Status {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object idObj = response.getBody().get("id");
                if (idObj instanceof Number) {
                    long id = ((Number) idObj).longValue();

                    log.info("Zahlung genehmigt | User-ID {} | amount {}", userId, amount);
                    return true;
                } else {
                    log.warn("Payment API returned non-numeric id: {}", idObj);
                    throw new IllegalStateException("Payment failed! Payment API returned non-numeric id " + idObj);
                }
            } else {
                log.warn("Payment API not successful: status={} body={}", response.getStatusCode(), response.getBody());
                throw new HttpClientErrorException(response.getStatusCode(), "Payment failed!");
            }
        } catch (Exception e) {
            log.warn("Payment not possible because of Network error");
            // Fallback für Fehlerszenarien (z. B. Netzwerkfehler)
            throw new RuntimeException("Payment failed! ", e);
        }
    }
}
