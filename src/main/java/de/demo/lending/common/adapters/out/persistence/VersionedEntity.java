package de.demo.lending.common.adapters.out.persistence;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public class VersionedEntity {

    @Version
    private Long version;

    public Long getVersion() {
        return version;
    }
}
