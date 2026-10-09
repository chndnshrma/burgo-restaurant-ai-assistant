package com.burgo.api.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservation", indexes = @Index(name = "idx_reservation_slot", columnList = "restaurant_id, booking_date, booking_time"))
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(nullable = false, length = 80)
    private String guestName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "booking_time", nullable = false)
    private LocalTime bookingTime;

    private int partySize;

    @Column(length = 300)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Reservation() {
    }

    public Reservation(String reference, Restaurant restaurant, String guestName, String phone,
                       LocalDate bookingDate, LocalTime bookingTime, int partySize, String notes) {
        this.reference = reference;
        this.restaurant = restaurant;
        this.guestName = guestName;
        this.phone = phone;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.partySize = partySize;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public String getGuestName() { return guestName; }
    public LocalDate getBookingDate() { return bookingDate; }
    public LocalTime getBookingTime() { return bookingTime; }
    public int getPartySize() { return partySize; }
    public ReservationStatus getStatus() { return status; }
}
