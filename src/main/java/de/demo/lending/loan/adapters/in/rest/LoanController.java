package de.demo.lending.loan.adapters.in.rest;

import de.demo.lending.loan.adapters.in.rest.dto.ReserveBookRequest;
import de.demo.lending.loan.application.CreateLoan;
import de.demo.lending.loan.application.command.ReserveBookCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/*
    Der Rest Controller ist der Inbound Adapter in unserer hexagonalen Architektur.
    Er verwendet den Inbound Port (dieser liegt im Domain core), um den Use Case zu triggern.
    Der UseCase Service implmentiert den Inbound Port und referenziert das Aggregat.
    Damit ist die Abhängigkeit von Außen nach Innen gerichtet und der Domain core kommt komplett
    ohne Abhängigkeiten aus.
 */
@RestController
@RequestMapping("/loans")
public class LoanController {
    private static final Logger log = LoggerFactory.getLogger(LoanController.class);
    private final CreateLoan createLoan;

    public LoanController(CreateLoan createLoan) {
        this.createLoan = createLoan;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody ReserveBookRequest request) {
        log.debug("Received create loan request with body: {}", request);

        log.debug("userId from request: {}", request.userId());
        log.debug("isbn from request: {}", request.isbn());

        // Beginn einer neuen Kette
        String correlationId = UUID.randomUUID().toString();
        String causationId = correlationId;  // erste Ursache = der Request selbst

        ReserveBookCommand cmd = new ReserveBookCommand(request.userId(), request.isbn());
        var id = createLoan.handle(cmd, correlationId, causationId);
        return ResponseEntity.accepted().body(Map.of("loanId", id.toString()));
    }
}
