package com.crud.rental.service;

import com.crud.rental.domain.*;
import com.crud.rental.repository.*;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final OptionRepository optionRepository;
    private final ReservationRepository reservationRepository;

    public PaymentService(CarRepository carRepository, UserRepository userRepository,
                          OptionRepository optionRepository, ReservationRepository reservationRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.optionRepository = optionRepository;
        this.reservationRepository = reservationRepository;
    }

    public String createCheckoutSession(CheckoutRequest req) throws Exception {
        Stripe.apiKey = stripeSecretKey;

        Car car = carRepository.findById(req.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found"));

        LocalDate start = LocalDate.parse(req.getStartDate());
        LocalDate end = LocalDate.parse(req.getEndDate());
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) throw new RuntimeException("Invalid date range");

        BigDecimal carTotal = car.getPrice().multiply(BigDecimal.valueOf(days));

        BigDecimal optionsTotal = BigDecimal.ZERO;
        if (req.getOptionIds() != null && !req.getOptionIds().isEmpty()) {
            List<Option> options = optionRepository.findAllById(req.getOptionIds());
            optionsTotal = options.stream()
                    .map(Option::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .multiply(BigDecimal.valueOf(days));
        }

        BigDecimal total = carTotal.add(optionsTotal);
        long amountInGrosze = total.multiply(BigDecimal.valueOf(100)).longValue();

        String optionIdsStr = (req.getOptionIds() != null && !req.getOptionIds().isEmpty())
                ? req.getOptionIds().stream().map(String::valueOf).collect(Collectors.joining(","))
                : "";

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/reservations/new")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("pln")
                                .setUnitAmount(amountInGrosze)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Wynajem: " + car.getCarBrand() + " (" + days + " " + (days == 1 ? "dzień" : "dni") + ")")
                                        .build())
                                .build())
                        .build())
                .putMetadata("userId", String.valueOf(req.getUserId()))
                .putMetadata("carId", String.valueOf(req.getCarId()))
                .putMetadata("startDate", req.getStartDate())
                .putMetadata("endDate", req.getEndDate())
                .putMetadata("optionIds", optionIdsStr)
                .putMetadata("totalPrice", total.toPlainString())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    @Transactional
    public Map<String, Object> confirmCheckoutSession(String sessionId) throws Exception {
        Stripe.apiKey = stripeSecretKey;

        // Idempotency: jeśli webhook już utworzył rezerwację, zwróć ją
        var existing = reservationRepository.findByStripeSessionId(sessionId);
        if (existing.isPresent()) {
            return Map.of("reservationId", existing.get().getId());
        }

        Session session = Session.retrieve(sessionId);
        if (!"paid".equals(session.getPaymentStatus())) {
            throw new RuntimeException("Payment not completed");
        }

        Reservation reservation = buildReservation(session.getMetadata(), session.getId());
        reservationRepository.save(reservation);
        return Map.of("reservationId", reservation.getId());
    }

    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) throws Exception {
        if ("PLACEHOLDER".equals(webhookSecret)) {
            return;
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid Stripe signature", e);
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Cannot deserialize Stripe event"));

            if (reservationRepository.findByStripeSessionId(session.getId()).isPresent()) {
                return;
            }

            Reservation reservation = buildReservation(session.getMetadata(), session.getId());
            reservationRepository.save(reservation);
        }
    }

    private Reservation buildReservation(Map<String, String> metadata, String sessionId) {
        Long userId = Long.parseLong(metadata.get("userId"));
        Long carId = Long.parseLong(metadata.get("carId"));
        LocalDate startDate = LocalDate.parse(metadata.get("startDate"));
        LocalDate endDate = LocalDate.parse(metadata.get("endDate"));
        BigDecimal totalPrice = new BigDecimal(metadata.get("totalPrice"));
        String optionIdsStr = metadata.getOrDefault("optionIds", "");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        List<Option> options = new ArrayList<>();
        if (!optionIdsStr.isBlank()) {
            List<Long> ids = Arrays.stream(optionIdsStr.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            options = new ArrayList<>(optionRepository.findAllById(ids));
        }

        car.setAvailability(false);
        carRepository.save(car);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setCar(car);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus(true);
        reservation.setMileage(0);
        reservation.setEnded(false);
        reservation.setOptions(options);
        reservation.setDamages(new ArrayList<>());
        reservation.setPaymentStatus("PAID");
        reservation.setStripeSessionId(sessionId);
        return reservation;
    }
}
