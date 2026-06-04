package de.demo.lending.catalog.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringCatalogBookRepository extends JpaRepository<CatalogBookEntity, UUID> {
}
