package com.crud.rental.mapper;

import com.crud.rental.domain.Option;
import com.crud.rental.domain.Reservation;
import com.crud.rental.domain.ReservationDto;
import com.crud.rental.repository.OptionRepository;
import com.crud.rental.service.CarService;
import com.crud.rental.service.OptionService;
import com.crud.rental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationMapper {

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private OptionService optionService;

    public Reservation mapToReservation(ReservationDto reservationDto) {
        Reservation reservation = new Reservation();
        reservation.setId(reservationDto.getId());
        reservation.setStartDate(reservationDto.getStartDate());
        reservation.setEndDate(reservationDto.getEndDate());
        reservation.setTotalPrice(reservationDto.getTotalPrice());
        reservation.setStatus(reservationDto.isStatus());

        reservation.setUser(userService.getUserById(reservationDto.getUserId()));
        reservation.setCar(carService.getCarById(reservationDto.getCarId()));
        List<Option> options = reservationDto.getOptionNames() != null ?
                reservationDto.getOptionNames().stream()
                        .map(optionService::getOptionByName)
                        .collect(Collectors.toList()) : new ArrayList<>();
        reservation.setOptions(options);

        return reservation;
    }

    public ReservationDto mapToReservationDto(Reservation reservation) {
        List<Long> optionIds = reservation.getOptions().stream()
                .map(Option::getId)
                .collect(Collectors.toList());

        List<String> optionNames = reservation.getOptions().stream()
                .map(Option::getName)
                .collect(Collectors.toList());

        return new ReservationDto(
                reservation.getId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getTotalPrice(),
                reservation.isStatus(),
                reservation.getUser().getId(),
                reservation.getCar().getId(),
                optionIds,
                optionNames,
                reservation.getUser().getUsername()
        );
    }

    public List<ReservationDto> mapToReservationDtoList(List<Reservation> reservationList) {
        return reservationList.stream()
                .map(this::mapToReservationDto)
                .collect(Collectors.toList());
    }
}
