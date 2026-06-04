package de.demo.lending.catalog.application.ports.out;

import de.demo.lending.catalog.domain.CatalogBook;
import de.demo.lending.catalog.domain.CatalogBookId;

import java.util.List;
import java.util.Optional;

public interface CatalogBookRepository {
    void save(CatalogBook book);
    Optional<CatalogBook> findById(CatalogBookId id);
    List<CatalogBook> findAll();
    void deleteById(CatalogBookId id);
}
