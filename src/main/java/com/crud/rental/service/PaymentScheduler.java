package com.crud.rental.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentScheduler {

    private final PaymentService paymentService;

    public PaymentScheduler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Co 5 minut: zwolnij auta z wygasłymi sesjami płatności
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void releaseExpiredPendingReservations() {
        paymentService.findExpiredPendingReservations()
                .forEach(paymentService::releaseExpiredPending);
    }
}
