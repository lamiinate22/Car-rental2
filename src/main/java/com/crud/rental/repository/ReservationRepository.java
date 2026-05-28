package com.crud.rental.repository;

import com.crud.rental.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findById(Long reservationId);
    Optional<Reservation> findByStripeSessionId(String stripeSessionId);
    Optional<Reservation> findByCar_IdAndPaymentStatus(Long carId, String paymentStatus);
    List<Reservation> findByPaymentStatusAndStripeSessionExpiresAtBefore(String paymentStatus, LocalDateTime dateTime);
}
