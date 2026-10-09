package com.burgo.api.domain;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One restaurant. Every menu item, booking and order hangs off one, which is what makes this multi-tenant later. */
@Entity
@Table(name = "restaurant")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String slug;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 200)
    private String tagline;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false)
    private LocalTime opens;

    @Column(nullable = false)
    private LocalTime closes;

    private int seatCapacity;
    private int slotMinutes;
    private int deliveryRadiusKm;
    private int minDeliveryOrderInr;
    private int deliveryFeeInr;
    private int freeDeliveryAboveInr;

    @Column(length = 300)
    private String parkingNote;

    protected Restaurant() {
    }

    public Restaurant(String slug, String name, String tagline, String address, String phone,
                      LocalTime opens, LocalTime closes, int seatCapacity, int slotMinutes,
                      int deliveryRadiusKm, int minDeliveryOrderInr, int deliveryFeeInr,
                      int freeDeliveryAboveInr, String parkingNote) {
        this.slug = slug;
        this.name = name;
        this.tagline = tagline;
        this.address = address;
        this.phone = phone;
        this.opens = opens;
        this.closes = closes;
        this.seatCapacity = seatCapacity;
        this.slotMinutes = slotMinutes;
        this.deliveryRadiusKm = deliveryRadiusKm;
        this.minDeliveryOrderInr = minDeliveryOrderInr;
        this.deliveryFeeInr = deliveryFeeInr;
        this.freeDeliveryAboveInr = freeDeliveryAboveInr;
        this.parkingNote = parkingNote;
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getTagline() { return tagline; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public LocalTime getOpens() { return opens; }
    public LocalTime getCloses() { return closes; }
    public int getSeatCapacity() { return seatCapacity; }
    public int getSlotMinutes() { return slotMinutes; }
    public int getDeliveryRadiusKm() { return deliveryRadiusKm; }
    public int getMinDeliveryOrderInr() { return minDeliveryOrderInr; }
    public int getDeliveryFeeInr() { return deliveryFeeInr; }
    public int getFreeDeliveryAboveInr() { return freeDeliveryAboveInr; }
    public String getParkingNote() { return parkingNote; }
}
