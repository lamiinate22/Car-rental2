package com.crud.rental.frontend.views;

import com.crud.rental.domain.NbpRateDto;
import com.crud.rental.service.NbpService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("nbp-rates")
public class NbpRateView extends VerticalLayout {

    public NbpRateView(NbpService nbpService) {
        H2 title = new H2("Exchange Rates (NBP)");
        add(title);

        for (String code : new String[]{"EUR", "USD"}) {
            add(buildRateCard(nbpService, code));
        }
    }

    private VerticalLayout buildRateCard(NbpService nbpService, String code) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("max-width", "340px");

        Span currencyLabel = new Span("Loading " + code + "...");
        Span rateLabel = new Span();
        Span dateLabel = new Span();
        Button refreshBtn = new Button("Refresh", e -> loadRate(nbpService, code, currencyLabel, rateLabel, dateLabel));

        loadRate(nbpService, code, currencyLabel, rateLabel, dateLabel);

        card.add(currencyLabel, rateLabel, dateLabel, refreshBtn);
        return card;
    }

    private void loadRate(NbpService nbpService, String code,
                          Span currencyLabel, Span rateLabel, Span dateLabel) {
        try {
            NbpRateDto rate = nbpService.getRate(code);
            currencyLabel.setText(rate.getCurrency() + " (" + rate.getCode() + ")");
            rateLabel.setText("Mid rate: " + rate.getMid() + " PLN");
            dateLabel.setText("Effective date: " + rate.getEffectiveDate());
        } catch (Exception e) {
            currencyLabel.setText(code + " — failed to load");
            rateLabel.setText("");
            dateLabel.setText("");
        }
    }
}
