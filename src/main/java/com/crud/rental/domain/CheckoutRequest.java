package com.crud.rental.domain;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {
    private Long userId;
    private Long carId;
    private String startDate;
    private String endDate;
    private List<Long> optionIds;
}
