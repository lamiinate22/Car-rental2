package com.crud.rental.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "RESERVATIONS")
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDate endDate;

    @Column(name = "TOTAL_PRICE")
    private BigDecimal totalPrice;

    @Column(name = "STATUS", nullable = false)
    private boolean status;

    @Column(name = "MILEAGE", nullable = false)
    private int mileage;

    @Column(name = "ENDED", nullable = false)
    private boolean ended;

    @Column(name = "PAYMENT_STATUS")
    private String paymentStatus;

    @Column(name = "STRIPE_SESSION_ID")
    private String stripeSessionId;

    @Column(name = "STRIPE_SESSION_URL", length = 1024)
    private String stripeSessionUrl;

    @Column(name = "STRIPE_SESSION_EXPIRES_AT")
    private LocalDateTime stripeSessionExpiresAt;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "CAR_ID")
    private Car car;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<Damage> damages;


    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "RESERVATION_OPTION",
            joinColumns = @JoinColumn(name = "RESERVATION_ID"),
            inverseJoinColumns = @JoinColumn(name = "OPTION_ID")
    )
    private List<Option> options;

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                ", mileage=" + mileage +
                ", ended=" + ended +
                ", car=" + (car != null ? car.getCarBrand() : "null") +
                '}';
    }
}
