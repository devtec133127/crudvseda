package de.demo.lending.loan.adapters.in.demo;

import java.util.UUID;

import de.demo.lending.common.application.ports.out.DemoEventStore;
import de.demo.lending.loan.adapters.in.demo.dto.DemoLoanResponse;
import de.demo.lending.loan.adapters.in.rest.dto.ReserveBookRequest;
import de.demo.lending.loan.application.CreateLoan;
import de.demo.lending.loan.application.command.ReserveBookCommand;
import de.demo.lending.loan.domain.Loan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Demo-Controller für die Präsentations-UI.
 * Zeigt Event-Driven Architecture mit Server-Sent Events.
 * <p>
 * NICHT für Production gedacht!
 */
@RestController
@RequestMapping("/api/demo")
public class DemoLoanController {
    private final DemoEventStore eventStore;
    private final CreateLoan createLoanUseCase;

    public DemoLoanController(CreateLoan createLoanUseCase,
                              DemoEventStore eventStore) {
        this.createLoanUseCase = createLoanUseCase;
        this.eventStore = eventStore;
    }

    @PostMapping("/loans")
    public ResponseEntity<DemoLoanResponse> create(@RequestBody ReserveBookRequest request) {
        // Beginn einer neuen Kette
        String correlationId = UUID.randomUUID().toString();
        String causationId = correlationId;  // erste Ursache = der Request selbst

        ReserveBookCommand cmd = new ReserveBookCommand(request.userId(), request.isbn());
        Loan loan = this.createLoanUseCase.handle(cmd, correlationId, causationId);

        DemoLoanResponse response = DemoLoanResponse.from(loan);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/loans/{loanId}/events",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable String loanId) {
        return eventStore.subscribe(loanId);
    }
}
