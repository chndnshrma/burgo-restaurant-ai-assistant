package com.burgo.api.domain;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MenuCategory category;

    private int priceInr;
    private boolean veg;
    /** 0 = not spicy, 1 = mild, 2 = spicy, 3 = very spicy. */
    private int spiceLevel;
    private boolean bestSeller;
    private int calories;
    private boolean available = true;

    protected MenuItem() {
    }

    public MenuItem(Restaurant restaurant, String name, String description, MenuCategory category,
                    int priceInr, boolean veg, int spiceLevel, boolean bestSeller, int calories) {
        this.restaurant = restaurant;
        this.name = name;
        this.description = description;
        this.category = category;
        this.priceInr = priceInr;
        this.veg = veg;
        this.spiceLevel = spiceLevel;
        this.bestSeller = bestSeller;
        this.calories = calories;
    }

    public Long getId() { return id; }
    public Restaurant getRestaurant() { return restaurant; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public MenuCategory getCategory() { return category; }
    public int getPriceInr() { return priceInr; }
    public boolean isVeg() { return veg; }
    public int getSpiceLevel() { return spiceLevel; }
    public boolean isBestSeller() { return bestSeller; }
    public int getCalories() { return calories; }
    public boolean isAvailable() { return available; }
}
