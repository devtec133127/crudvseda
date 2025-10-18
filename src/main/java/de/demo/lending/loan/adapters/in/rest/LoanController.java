package de.demo.lending.loan.adapters.in.rest;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.application.CreateLoan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/loans")
@ConditionalOnProperty(value="service.role", havingValue="loan")
public class LoanController {
    private static final Logger log = LoggerFactory.getLogger(LoanController.class);
    private final CreateLoan createLoan;
    public LoanController(CreateLoan createLoan) { this.createLoan = createLoan; }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String,String> body) {
        log.debug("Received create loan request with body: {}", body);

        String userId = body.get("userId");
        String bookTitle = body.get("bookTitle");

        log.debug("userId from request: {}", userId);
        log.debug("bookTitle from request: {}", bookTitle);

        UUID userIdFromRequest = UUID.fromString(body.get("userId"));

        // Beginn einer neuen Kette
        String correlationId = UUID.randomUUID().toString();
        String causationId   = correlationId;  // erste Ursache = der Request selbst

        var id = createLoan.handle(UserId.of(userIdFromRequest), bookTitle, correlationId, causationId);
        return ResponseEntity.accepted().body(Map.of("loanId", id.toString()));
    }
}
