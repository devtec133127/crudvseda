package de.demo.lending.loan.adapters.in.rest;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.application.CreateLoan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/loans")
@ConditionalOnProperty(value="service.role", havingValue="loan")
public class LoanController {
    private final CreateLoan createLoan;
    public LoanController(CreateLoan createLoan) { this.createLoan = createLoan; }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String,String> body) {
        UUID userIdFromRequest = UUID.fromString(body.get("id"));
        UUID bookIdFromRequest = UUID.fromString(body.get("bookId"));

        // Beginn einer neuen Kette
        String correlationId = UUID.randomUUID().toString();
        String causationId   = correlationId;  // erste Ursache = der Request selbst

        var id = createLoan.handle(UserId.of(userIdFromRequest), BookId.of(bookIdFromRequest), correlationId, causationId);
        return ResponseEntity.accepted().body(Map.of("loanId", id.toString()));
    }
}
