package de.demo.lending.repository;

import de.demo.lending.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    Optional<Book> findByTitle(String title);
    Iterable<Book> findByUserId(UUID userId);
    Iterable<Book> findAllByAvailable(boolean available);
}
