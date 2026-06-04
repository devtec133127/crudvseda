package de.demo.lending.catalog.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "catalog_book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogBookEntity {

    @Id
    private UUID id;

    private String isbn;

    @Column(nullable = false)
    private String title;

    private String author;

    private String coverUrl;

    private String openLibraryKey;

    @Column(nullable = false)
    private Instant addedAt;
}
