package de.demo.lending.repository;

import de.demo.lending.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    Iterable<Book> findByUserId(UUID userId);
    Iterable<Book> findAllByAvailable(boolean available);
    List<Book> findByTitleContainingIgnoreCase(String title);
}
