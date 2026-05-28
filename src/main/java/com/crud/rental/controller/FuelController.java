package com.crud.rental.controller;

import com.crud.rental.domain.FuelPriceDto;
import com.crud.rental.domain.NbpRateDto;
import com.crud.rental.service.FuelUsageService;
import com.crud.rental.service.NbpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/fuels")
@RequiredArgsConstructor
public class FuelController {
    private final FuelUsageService fuelService;
    private final NbpService nbpService;

    @GetMapping("/prices")
    public ResponseEntity<List<FuelPriceDto>> getFuelPrices() {
        try {
            List<FuelPriceDto> fuelPrices = fuelService.getFuelPrices();
            return ResponseEntity.ok(fuelPrices);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/types")
    public List<String> getFuelTypes() {
        return List.of("95", "98", "ON", "ON+", "LPG");
    }

    @GetMapping("/nbp-rate/{code}")
    public ResponseEntity<NbpRateDto> getNbpRate(@PathVariable String code) {
        try {
            return ResponseEntity.ok(nbpService.getRate(code));
        } catch (Exception e) {
            return ResponseEntity.status(502).build();
        }
    }
}
