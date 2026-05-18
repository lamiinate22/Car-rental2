package com.crud.rental.service;

import com.crud.rental.domain.*;
import com.crud.rental.exception.CarNotFoundException;
import com.crud.rental.exception.OptionNotFoundException;
import com.crud.rental.exception.ReservationNotFoundException;
import com.crud.rental.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private DamageRespository damageRespository;

    @Autowired
    private OptionService optionService;

    @Transactional
    public void addReservation(Reservation reservation) {
        // Pobierz użytkownika i samochód z bazy danych
        User user = userRepository.findById(reservation.getUser().getId()).orElseThrow(() -> new RuntimeException("User not found"));
        Car car = carRepository.findById(reservation.getCar().getId()).orElseThrow(() -> new RuntimeException("Car not found"));

        List<Option> managedOptions = reservation.getOptions().stream()
                .map(option -> optionRepository.findById(option.getId()).orElseThrow())
                .collect(Collectors.toList());
        reservation.setOptions(managedOptions);

        // Ustaw pobrane obiekty na rezerwację
        reservation.setUser(user);
        reservation.setCar(car);

        // Oznacz samochód jako niedostępny
        car.setAvailability(false);
        carRepository.save(car);

        // Zapisz rezerwację
        reservationRepository.save(reservation);
    }

    public Reservation addCarToReservation(final Long carId, final Long reservationId) throws ReservationNotFoundException, CarNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);
        Car car = carRepository.findById(carId).orElseThrow(ReservationNotFoundException::new);
        if (!car.isAvailability()) {
            throw new IllegalStateException("Samochód nie jest dostępny.");
        }
        reservation.setCar(car);
        return reservationRepository.save(reservation);
    }

    public Reservation deleteCarFromReservation(final Long reservationId, final Long carId) throws ReservationNotFoundException, CarNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);


        Car car = reservation.getCar();
        if (car != null && car.getId().equals(carId)) {
            reservation.setCar(null);
            car.setAvailability(true);

            // Dodaj logi
            System.out.println("Car before save: " + car);
            car = carRepository.save(car);
            System.out.println("Car after save: " + car);
        } else {
            throw new CarNotFoundException();
        }

        return reservationRepository.save(reservation);
    }

    public Reservation getReservationById(final Long reservationId) throws ReservationNotFoundException {
        return reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);
    }

    public BigDecimal calculateDamageCosts(Reservation reservation) {
        List<Damage> damages = reservation.getDamages();
        BigDecimal totalDamageCosts = BigDecimal.ZERO;
        for (Damage damage : damages) {
            BigDecimal repairCost = damage.getRepairCost();
            totalDamageCosts = totalDamageCosts.add(repairCost);
        }
        return totalDamageCosts;
    }

    public BigDecimal calculateBasePrice(Reservation reservation) {
        Car car = reservation.getCar();
        LocalDate startDate = reservation.getStartDate();
        LocalDate endDate = reservation.getEndDate();

        long numberOfDays = ChronoUnit.DAYS.between(startDate, endDate);
        BigDecimal pricePerDay = car.getPrice();
        BigDecimal totalPrice = pricePerDay.multiply(BigDecimal.valueOf(numberOfDays));
        return totalPrice;
    }

    public BigDecimal calculateOptionCosts(Reservation reservation) {
        List<Option> options = reservation.getOptions();
        BigDecimal totalOptionCosts = BigDecimal.ZERO;
        if (options != null) {
            for (Option option : options) {
                BigDecimal optionPrice = option.getPrice();
                totalOptionCosts = totalOptionCosts.add(optionPrice);
            }
        }
        return totalOptionCosts;
    }

    public BigDecimal calculateTotalPrice(Long reservationId) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);
        BigDecimal basePrice = calculateBasePrice(reservation);
        BigDecimal optionCosts = calculateOptionCosts(reservation);
        BigDecimal damageCosts = calculateDamageCosts(reservation);
        BigDecimal totalPrice = basePrice.add(optionCosts).add(damageCosts);
        reservation.setTotalPrice(totalPrice);
        reservationRepository.save(reservation);

        return totalPrice;
    }



    public Reservation addOptionToReservation(Long reservationId, Long optionId) throws ReservationNotFoundException, OptionNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);
        Option option = optionService.getOptionById(optionId);
        List<Option> options = reservation.getOptions();
        if (options == null) {
            options = new ArrayList<>();
        }
        options.add(option);
        reservation.setOptions(options);
        return reservationRepository.save(reservation);
    }

    public Reservation deleteOptionFromReservation(final  long reservationId, final Long optionId) throws  ReservationNotFoundException, OptionNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);
        List<Option> options = reservation.getOptions();
        for (Option option : options) {
            if (option.getId() == optionId) {
                options.remove(option);
                reservation.setOptions(options);
                return reservationRepository.save(reservation);

            }
        }
        throw new OptionNotFoundException();
    }

    public void addDamageToReservation(Long reservationId, Long damageId) throws ReservationNotFoundException{
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);
        Damage damage  = damageRespository.findById(damageId).orElseThrow(() -> new RuntimeException("No fault with the given ID was found"));
        List<Damage> damages = reservation.getDamages();
        if (damages == null) {
            damages = new ArrayList<>();
        }
        damages.add(damage);
        reservation.setDamages(damages);
        reservationRepository.save(reservation);
    }

    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Reservation not found"));
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public void updateReservation(Reservation reservation) {
        addReservation(reservation);
    }

    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Reservation not found"));
        Car car = reservation.getCar();
        if (car != null) {
            car.setAvailability(true);
            carRepository.save(car);
        }
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void deleteAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        for (Reservation reservation : reservations) {
            Car car = reservation.getCar();
            if (car != null) {
                car.setAvailability(true);
                carRepository.save(car);
            }
        }
        reservationRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }


    public void endReservation(Long reservationId, int mileage, String region) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("Invalid reservation Id:" + reservationId));
        reservation.setMileage(mileage);
        reservation.setEnded(true); // Assuming you have a field to mark reservation as ended
        reservationRepository.save(reservation);
    }
}
