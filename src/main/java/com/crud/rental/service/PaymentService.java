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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final int SESSION_EXPIRY_MINUTES = 30;

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

    /**
     * Tworzy sesję Stripe Checkout i od razu blokuje auto rezerwacją PENDING.
     * Auto jest niedostępne przez max 30 minut (czas sesji Stripe).
     */
    @Transactional
    public String createCheckoutSession(CheckoutRequest req) throws Exception {
        Stripe.apiKey = stripeSecretKey;

        Car car = carRepository.findById(req.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found"));

        // Sprawdź czy auto nie jest zablokowane aktywną sesją płatności
        if (!car.isAvailability()) {
            var activePending = reservationRepository.findByCar_IdAndPaymentStatus(req.getCarId(), "PENDING");
            if (activePending.isPresent()) {
                Reservation pending = activePending.get();
                boolean sessionExpired = pending.getStripeSessionExpiresAt() != null
                        && pending.getStripeSessionExpiresAt().isBefore(LocalDateTime.now());

                if (sessionExpired) {
                    // Sesja wygasła — zwolnij auto i kontynuuj
                    releaseExpiredPending(pending);
                } else {
                    throw new RuntimeException("Ten samochód jest aktualnie rezerwowany przez innego użytkownika. Spróbuj ponownie za kilka minut.");
                }
            } else {
                throw new RuntimeException("Ten samochód jest niedostępny.");
            }
        }

        LocalDate start = LocalDate.parse(req.getStartDate());
        LocalDate end = LocalDate.parse(req.getEndDate());
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) throw new RuntimeException("Niepoprawny zakres dat.");

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal carTotal = car.getPrice().multiply(BigDecimal.valueOf(days));
        BigDecimal optionsTotal = BigDecimal.ZERO;
        List<Option> options = new ArrayList<>();

        if (req.getOptionIds() != null && !req.getOptionIds().isEmpty()) {
            options = new ArrayList<>(optionRepository.findAllById(req.getOptionIds()));
            optionsTotal = options.stream()
                    .map(Option::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .multiply(BigDecimal.valueOf(days));
        }

        BigDecimal total = carTotal.add(optionsTotal);
        long amountInGrosze = total.multiply(BigDecimal.valueOf(100)).longValue();

        String optionIdsStr = options.stream()
                .map(o -> String.valueOf(o.getId()))
                .collect(Collectors.joining(","));

        long expiresAtEpoch = Instant.now().getEpochSecond() + SESSION_EXPIRY_MINUTES * 60L;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setExpiresAt(expiresAtEpoch)
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

        // Auto zablokowane od razu — rezerwacja PENDING
        car.setAvailability(false);
        carRepository.save(car);

        Reservation pending = new Reservation();
        pending.setUser(user);
        pending.setCar(car);
        pending.setStartDate(start);
        pending.setEndDate(end);
        pending.setTotalPrice(total);
        pending.setStatus(false);
        pending.setMileage(0);
        pending.setEnded(false);
        pending.setOptions(options);
        pending.setDamages(new ArrayList<>());
        pending.setPaymentStatus("PENDING");
        pending.setStripeSessionId(session.getId());
        pending.setStripeSessionUrl(session.getUrl());
        pending.setStripeSessionExpiresAt(LocalDateTime.now().plusMinutes(SESSION_EXPIRY_MINUTES));
        reservationRepository.save(pending);

        return session.getUrl();
    }

    /**
     * Wywoływany przez stronę sukcesu. Weryfikuje płatność przez Stripe API
     * i aktualizuje istniejącą rezerwację PENDING → PAID.
     */
    @Transactional
    public Map<String, Object> confirmCheckoutSession(String sessionId) throws Exception {
        Stripe.apiKey = stripeSecretKey;

        Reservation reservation = reservationRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Reservation not found for this session"));

        // Idempotency — już opłacona
        if ("PAID".equals(reservation.getPaymentStatus())) {
            return Map.of("reservationId", reservation.getId());
        }

        Session session = Session.retrieve(sessionId);
        if (!"paid".equals(session.getPaymentStatus())) {
            throw new RuntimeException("Payment not completed");
        }

        markAsPaid(reservation);
        return Map.of("reservationId", reservation.getId());
    }

    /**
     * Wywoływany przez webhook Stripe. Aktualizuje PENDING → PAID.
     */
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

            reservationRepository.findByStripeSessionId(session.getId())
                    .filter(r -> !"PAID".equals(r.getPaymentStatus()))
                    .ifPresent(this::markAsPaid);
        }
    }

    /**
     * Zwalnia auto i oznacza rezerwację jako EXPIRED.
     * Wywoływane przez scheduler oraz reaktywnie przy nowej rezerwacji.
     */
    @Transactional
    public void releaseExpiredPending(Reservation reservation) {
        Car car = reservation.getCar();
        if (car != null) {
            car.setAvailability(true);
            carRepository.save(car);
        }
        reservation.setPaymentStatus("EXPIRED");
        reservation.setStatus(false);
        reservationRepository.save(reservation);
    }

    /**
     * Zwraca wszystkie wygasłe rezerwacje PENDING do zwolnienia przez scheduler.
     */
    public List<Reservation> findExpiredPendingReservations() {
        return reservationRepository.findByPaymentStatusAndStripeSessionExpiresAtBefore(
                "PENDING", LocalDateTime.now());
    }

    private void markAsPaid(Reservation reservation) {
        reservation.setPaymentStatus("PAID");
        reservation.setStatus(true);
        reservationRepository.save(reservation);
    }
}
