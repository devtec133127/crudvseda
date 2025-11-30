package de.demo.lending.inventory.adapters.out.persistence;

import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.inventory.domain.Reservation;
import de.demo.lending.inventory.domain.port.out.ReservationRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final SpringReservationRepository jpa;

    public ReservationRepositoryAdapter(SpringReservationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Reservation save(de.demo.lending.inventory.domain.Reservation reservation) {
        // map Domain -> Entity und save
        ReservationEntity e = new ReservationEntity();
        e.setId(reservation.getReservationId().value());
        e.setState(reservation.getStatus().name());
        e.setExpiresAt(reservation.getExpiresAt());
        e.setCreatedAt(reservation.getCreatedAt());
        e.setUserId(reservation.getUserId().value().toString());
        e.setCopyId(reservation.getCopyId().value().toString());

        ReservationEntity save = this.jpa.save(e);

        Duration duration = Duration.between(Instant.now(), save.getExpiresAt());
        // map Entity to Domain of saved entity
        return Reservation.create(reservation.getLoanId(),
                CopyId.of(UUID.fromString(save.getCopyId())),
                reservation.getUserId(),
                duration);
    }
}
