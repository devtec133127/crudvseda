package de.demo.lending.loan.adapters.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringLoanJpaRepository extends JpaRepository<LoanEntity, UUID> {
}
