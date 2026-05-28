package com.crud.rental.repository;

import com.crud.rental.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findById(Long reservationId);
    Optional<Reservation> findByStripeSessionId(String stripeSessionId);
}
