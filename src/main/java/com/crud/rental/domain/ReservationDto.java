package com.crud.rental.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ReservationDto {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private boolean status;
    private Long userId;
    private Long carId;
    private List<Long> optionIds;
    private List<String> optionNames = new ArrayList<>();
    private String username;
    private String paymentStatus;
    private String stripeSessionUrl;
    private LocalDateTime stripeSessionExpiresAt;
}
