package com.crud.rental.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NbpRateDto {
    private String currency;
    private String code;
    private BigDecimal mid;
    private String effectiveDate;
}
