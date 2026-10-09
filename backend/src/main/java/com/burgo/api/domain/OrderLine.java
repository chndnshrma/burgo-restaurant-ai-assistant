package com.burgo.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A snapshot of what was ordered, so later menu edits never rewrite an old bill. */
@Entity
@Table(name = "order_line")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private CustomerOrder order;

    @Column(nullable = false, length = 120)
    private String itemName;

    private int unitPriceInr;
    private int quantity;

    protected OrderLine() {
    }

    public OrderLine(String itemName, int unitPriceInr, int quantity) {
        this.itemName = itemName;
        this.unitPriceInr = unitPriceInr;
        this.quantity = quantity;
    }

    void attachTo(CustomerOrder order) {
        this.order = order;
    }

    public String getItemName() { return itemName; }
    public int getUnitPriceInr() { return unitPriceInr; }
    public int getQuantity() { return quantity; }
}
