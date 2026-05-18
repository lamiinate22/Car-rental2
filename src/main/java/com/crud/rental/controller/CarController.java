package com.crud.rental.controller;

import com.crud.rental.domain.Car;
import com.crud.rental.domain.CarDto;
import com.crud.rental.exception.CarNotFoundException;
import com.crud.rental.mapper.CarMapper;
import com.crud.rental.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;
    private final CarMapper carMapper;
    @GetMapping("/{available}")
    public ResponseEntity<List<CarDto>> getAvailableCars(){
        List<Car> availableCars = carService.getAvailableCars();
        List<CarDto> availableCarDtos = carMapper.mapToCarDtoList(availableCars);
        return ResponseEntity.ok(availableCarDtos);

    }
    @GetMapping
    public ResponseEntity<List<CarDto>> getAllCars(){
        List<Car> cars = carService.getAllCars();
        List<CarDto>carDtos = carMapper.mapToCarDtoList(cars);
        return ResponseEntity.ok(carDtos);
    }
    @PostMapping
    public ResponseEntity<Void> addCar(@RequestBody CarDto carDto){
        Car car = carMapper.mapToCar(carDto);
        carService.saveCar(car);
        return  ResponseEntity.ok().build();
    }
    @DeleteMapping(value = "{carId}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long carId) throws CarNotFoundException{
        carService.deleteCarById(carId);
        return ResponseEntity.ok().build();
    }
    @PutMapping
    public ResponseEntity<CarDto> updateCar(@RequestBody CarDto carDto) throws CarNotFoundException {
        Car car = carMapper.mapToCar(carDto);
        Car savedCar = carService.saveCar(car);
        return ResponseEntity.ok(carMapper.mapToCarDto(savedCar));
    }

}
