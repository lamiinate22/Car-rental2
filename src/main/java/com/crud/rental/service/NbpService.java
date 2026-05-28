package com.crud.rental.service;

import com.crud.rental.domain.NbpRateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NbpService {

    private static final String NBP_URL =
            "https://api.nbp.pl/api/exchangerates/rates/A/{code}/?format=json";

    private final RestTemplate restTemplate;

    public NbpRateDto getRate(String currencyCode) {
        NbpApiResponse response = restTemplate.getForObject(
                NBP_URL, NbpApiResponse.class, currencyCode.toUpperCase());

        if (response == null || response.rates == null || response.rates.isEmpty()) {
            throw new RuntimeException("No data from NBP API for: " + currencyCode);
        }

        NbpRate rate = response.rates.get(0);
        return new NbpRateDto(response.currency, response.code, rate.mid, rate.effectiveDate);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class NbpApiResponse {
        @JsonProperty("currency")  public String currency;
        @JsonProperty("code")      public String code;
        @JsonProperty("rates")     public List<NbpRate> rates;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class NbpRate {
        @JsonProperty("effectiveDate") public String effectiveDate;
        @JsonProperty("mid")           public BigDecimal mid;
    }
}
