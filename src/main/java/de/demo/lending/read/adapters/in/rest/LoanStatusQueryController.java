package de.demo.lending.read.adapters.in.rest;

import de.demo.lending.read.adapters.out.persistence.LoanStatusRead;
import de.demo.lending.read.application.LoanStatusQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class LoanStatusQueryController {

    private final LoanStatusQueryService service;

    public LoanStatusQueryController(LoanStatusQueryService service) {
        this.service = service;
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanStatusRead> getOrderStatus(@PathVariable UUID loanId) {
        Optional<LoanStatusRead> loanStatus = service.getLoanStatus(loanId);
        if (loanStatus.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.of(loanStatus);
    }

    @GetMapping("/customer/{customerId}")
    public List<LoanStatusRead> getOrdersForCustomer(@PathVariable UUID customerId) {
        return service.getLoansForCustomer(customerId);
    }
}
