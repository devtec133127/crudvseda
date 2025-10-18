package de.demo.lending.repository;

import de.demo.lending.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface LoanRepository extends JpaRepository<Loan, UUID> {
    // Optional: eigene Methoden
}
