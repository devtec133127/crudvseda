package de.demo.lending.catalog.adapters.out.persistence;

import de.demo.lending.catalog.application.ports.out.CatalogBookRepository;
import de.demo.lending.catalog.domain.CatalogBook;
import de.demo.lending.catalog.domain.CatalogBookId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CatalogBookRepositoryAdapter implements CatalogBookRepository {

    private final SpringCatalogBookRepository jpa;

    public CatalogBookRepositoryAdapter(SpringCatalogBookRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(CatalogBook book) {
        jpa.save(toEntity(book));
    }

    @Override
    public Optional<CatalogBook> findById(CatalogBookId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<CatalogBook> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(CatalogBookId id) {
        jpa.deleteById(id.value());
    }

    private CatalogBook toDomain(CatalogBookEntity e) {
        return CatalogBook.restore(
                CatalogBookId.of(e.getId()),
                e.getIsbn(),
                e.getTitle(),
                e.getAuthor(),
                e.getCoverUrl(),
                e.getOpenLibraryKey(),
                e.getAddedAt()
        );
    }

    private CatalogBookEntity toEntity(CatalogBook book) {
        return CatalogBookEntity.builder()
                .id(book.getId().value())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .coverUrl(book.getCoverUrl())
                .openLibraryKey(book.getOpenLibraryKey())
                .addedAt(book.getAddedAt())
                .build();
    }
}
