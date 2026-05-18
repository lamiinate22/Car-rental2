package com.crud.rental.controller;

import com.crud.rental.domain.Reservation;
import com.crud.rental.domain.ReservationDto;
import com.crud.rental.exception.ReservationNotFoundException;
import com.crud.rental.mapper.ReservationMapper;
import com.crud.rental.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationMapper reservationMapper;

    @PostMapping
    public ResponseEntity<Void> addReservation(@RequestBody ReservationDto reservationDto) {
        Reservation reservation = reservationMapper.mapToReservation(reservationDto);
        reservationService.addReservation(reservation);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "{reservationId}")
    public ResponseEntity<ReservationDto> getReservation(@PathVariable Long reservationId) throws ReservationNotFoundException {
        Reservation reservation = reservationService.getReservation(reservationId);
        ReservationDto reservationDto = reservationMapper.mapToReservationDto(reservation);
        return ResponseEntity.ok(reservationDto);
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<Void> updateReservation(@PathVariable Long reservationId, @RequestBody ReservationDto reservationDto) throws ReservationNotFoundException {
        Reservation reservation = reservationMapper.mapToReservation(reservationDto);
        reservation.setId(reservationId);
        reservationService.updateReservation(reservation);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long reservationId) throws ReservationNotFoundException {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add/{reservationId}/{carId}")
    public ResponseEntity<ReservationDto> addCarToReservation(@PathVariable Long reservationId, @PathVariable Long carId) throws ReservationNotFoundException {
        Reservation reservation = reservationService.getReservation(reservationId);
        // Logika dodawania samochodu do rezerwacji
        return ResponseEntity.ok(reservationMapper.mapToReservationDto(reservation));
    }
}
