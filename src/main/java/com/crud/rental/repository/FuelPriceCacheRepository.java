package com.crud.rental.repository;

import com.crud.rental.domain.FuelPriceCacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuelPriceCacheRepository extends JpaRepository<FuelPriceCacheEntry, Long> {
}
