package com.crud.rental.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "FUEL_PRICE_CACHE")
@NoArgsConstructor
@AllArgsConstructor
public class FuelPriceCacheEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "REGION", nullable = false)
    private String region;

    @Column(name = "FUEL_TYPE", nullable = false)
    private String fuelType;

    @Column(name = "PRICE", nullable = false)
    private BigDecimal price;

    @Column(name = "CACHED_AT", nullable = false)
    private LocalDateTime cachedAt;

    public FuelPriceCacheEntry(String region, String fuelType, BigDecimal price, LocalDateTime cachedAt) {
        this.region = region;
        this.fuelType = fuelType;
        this.price = price;
        this.cachedAt = cachedAt;
    }
}
