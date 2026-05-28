package com.crud.rental.frontend.views;

import com.crud.rental.domain.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        H1 header = new H1("Welcome to Car Rental Service");

        Paragraph description = new Paragraph(
                "Welcome to our Car Rental Service. We offer a wide range of vehicles for all your needs. " +
                        "Whether you're looking for a compact car for city driving or a spacious SUV for a family trip, " +
                        "we've got you covered. Check out our fleet, find out the latest fuel prices, and book your rental car today!"
        );

        RouterLink carsLink = new RouterLink("Browse Our Cars", CarsView.class);
        RouterLink fuelPricesLink = new RouterLink("Fuel Prices", FuelPricesView.class);
        RouterLink nbpRatesLink = new RouterLink("Exchange Rates (NBP)", NbpRateView.class);
        RouterLink rentalFormLink = new RouterLink("Rental Form", RentalFormView.class);
        RouterLink reservationsLink = new RouterLink("Reservations", AllReservationsView.class);
        RouterLink loginLink = new RouterLink("Login", LoginView.class);
        RouterLink registerLink = new RouterLink("Register", RegisterView.class);
        Button logoutButton = new Button("Logout", e -> {
            VaadinSession.getCurrent().close();
        });

        HorizontalLayout linksLayout = null;

        var loggedUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (loggedUser != null)
        {
            linksLayout = new HorizontalLayout(carsLink, fuelPricesLink, nbpRatesLink, rentalFormLink, reservationsLink, logoutButton);
        }
        else
        {
            linksLayout = new HorizontalLayout(carsLink, fuelPricesLink, nbpRatesLink, rentalFormLink, reservationsLink, loginLink, registerLink);
        }

        linksLayout.addClassName("links-layout");

        add(header, description, linksLayout);

        addClassName("main-view");
    }
}
