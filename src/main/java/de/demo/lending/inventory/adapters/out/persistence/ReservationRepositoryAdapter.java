package de.demo.lending.inventory.adapters.out.persistence;

import de.demo.lending.inventory.application.ReservationRepository;
import de.demo.lending.inventory.domain.Reservation;
import org.springframework.stereotype.Component;

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
        e.setBookTitle(reservation.getBookTitle());
        e.setBookId(reservation.getBookId().value());
        e.setUserId(reservation.getUserId().value().toString());
        e.setCopyId(reservation.getCopyId());

        ReservationEntity save = this.jpa.save(e);

        // map Entity to Domain of saved entity
        return Reservation.create(reservation.getReservationId().toString(),
                "",
                reservation.getBookTitle(),
                reservation.getBookId(),
                reservation.getUserId(),
                Reservation.ReservationStatus.valueOf(save.getState()),
                save.getCreatedAt(),
                save.getExpiresAt(),
                save.getCopyId());
    }
}
