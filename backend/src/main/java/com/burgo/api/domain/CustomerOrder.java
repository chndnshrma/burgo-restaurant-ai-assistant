package com.burgo.api.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.burgo.api.core.OrderType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/** Named CustomerOrder because ORDER is a reserved word in SQL. */
@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType type;

    @Column(nullable = false, length = 80)
    private String customerName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(length = 300)
    private String address;

    @Column(length = 300)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.RECEIVED;

    private int subtotalInr;
    private int taxInr;
    private int deliveryFeeInr;
    private int totalInr;
    private int etaMinutes;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> lines = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(String reference, Restaurant restaurant, OrderType type, String customerName,
                         String phone, String address, String notes, int subtotalInr, int taxInr,
                         int deliveryFeeInr, int totalInr, int etaMinutes) {
        this.reference = reference;
        this.restaurant = restaurant;
        this.type = type;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.notes = notes;
        this.subtotalInr = subtotalInr;
        this.taxInr = taxInr;
        this.deliveryFeeInr = deliveryFeeInr;
        this.totalInr = totalInr;
        this.etaMinutes = etaMinutes;
    }

    public void addLine(OrderLine line) {
        line.attachTo(this);
        lines.add(line);
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public OrderType getType() { return type; }
    public OrderStatus getStatus() { return status; }
    public int getSubtotalInr() { return subtotalInr; }
    public int getTaxInr() { return taxInr; }
    public int getDeliveryFeeInr() { return deliveryFeeInr; }
    public int getTotalInr() { return totalInr; }
    public int getEtaMinutes() { return etaMinutes; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderLine> getLines() { return lines; }
}
