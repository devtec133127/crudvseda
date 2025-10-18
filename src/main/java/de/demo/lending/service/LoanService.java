package de.demo.lending.service;

import de.demo.lending.domain.Book;
import de.demo.lending.domain.Loan;
import de.demo.lending.repository.BookRepository;
import de.demo.lending.repository.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    public Loan createLoan(UUID userId, Book book) {

        // 2. Loan erstellen
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUserId(userId);
        loan.setStatus("ACTIVE");
        loan.setStartDate(LocalDate.now());
        loan = loanRepository.save(loan);

        return loan;
    }
}