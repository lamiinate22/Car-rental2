package com.crud.rental.frontend.views;

import com.crud.rental.domain.FuelPriceDto;
import com.crud.rental.domain.NbpRateDto;
import com.crud.rental.service.FuelUsageService;
import com.crud.rental.service.NbpService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("fuel-prices")
public class FuelPricesView extends VerticalLayout {

    @Autowired
    private FuelUsageService fuelService;

    @Autowired
    private NbpService nbpService;

    private Grid<RegionFuelPrices> grid = new Grid<>(RegionFuelPrices.class);

    public FuelPricesView(FuelUsageService fuelService, NbpService nbpService) {
        this.fuelService = fuelService;
        this.nbpService = nbpService;

        add(new H3("Exchange Rates (NBP)"));
        add(buildRatesRow());
        add(grid);

        refreshGrid();
    }

    private HorizontalLayout buildRatesRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        for (String code : new String[]{"EUR", "USD"}) {
            row.add(buildRateCard(code));
        }
        return row;
    }

    private VerticalLayout buildRateCard(String code) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("padding", "12px")
                .set("min-width", "200px");
        card.setSpacing(false);
        card.setPadding(false);

        Span nameSpan = new Span(code);
        nameSpan.getStyle().set("font-weight", "bold").set("font-size", "1.1em");
        Span rateSpan = new Span("loading...");
        Span dateSpan = new Span();
        dateSpan.getStyle().set("font-size", "0.85em").set("color", "#888");

        Button refresh = new Button("↻", e -> loadRate(code, nameSpan, rateSpan, dateSpan));
        refresh.getStyle().set("min-width", "0").set("padding", "2px 8px");

        loadRate(code, nameSpan, rateSpan, dateSpan);

        card.add(nameSpan, rateSpan, dateSpan, refresh);
        return card;
    }

    private void loadRate(String code, Span nameSpan, Span rateSpan, Span dateSpan) {
        try {
            NbpRateDto rate = nbpService.getRate(code);
            nameSpan.setText(rate.getCurrency() + " (" + rate.getCode() + ")");
            rateSpan.setText(rate.getMid() + " PLN");
            dateSpan.setText(rate.getEffectiveDate());
        } catch (Exception e) {
            rateSpan.setText("— unavailable");
            dateSpan.setText("");
        }
    }

    // Metoda odświeżająca dane w siatce
    private void refreshGrid() {
        try {
            List<FuelPriceDto> fuelPrices = fuelService.getFuelPrices();

            // Grupowanie danych według regionu
            Map<String, Map<String, BigDecimal>> regionFuelPriceMap = fuelPrices.stream()
                    .collect(Collectors.groupingBy(
                            FuelPriceDto::getRegion,
                            Collectors.toMap(FuelPriceDto::getFuelType, FuelPriceDto::getPrice)
                    ));

            // Konwersja danych na obiekty RegionFuelPrices dla siatki
            List<RegionFuelPrices> regionFuelPricesList = regionFuelPriceMap.entrySet().stream()
                    .map(entry -> new RegionFuelPrices(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            // Ustawienie danych w siatce
            grid.setItems(regionFuelPricesList);

            // Konfiguracja kolumn
            grid.removeAllColumns();
            grid.addColumn(RegionFuelPrices::getRegion).setHeader("Region");

            if (!regionFuelPricesList.isEmpty()) {
                RegionFuelPrices sample = regionFuelPricesList.get(0);
                sample.getFuelPrices().keySet().forEach(fuelType -> {
                    grid.addColumn(rfp -> {
                        BigDecimal price = rfp.getFuelPrices().get(fuelType);
                        return price != null && price.compareTo(BigDecimal.ZERO) != 0 ? price.toString() : "-";
                    }).setHeader(fuelType);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Wewnętrzna klasa reprezentująca dane dla siatki
    public static class RegionFuelPrices {
        private String region;
        private Map<String, BigDecimal> fuelPrices;

        public RegionFuelPrices(String region, Map<String, BigDecimal> fuelPrices) {
            this.region = region;
            this.fuelPrices = fuelPrices;
        }

        public String getRegion() {
            return region;
        }

        public Map<String, BigDecimal> getFuelPrices() {
            return fuelPrices;
        }
    }
}
