package com.burgo.api.core;

import java.time.LocalTime;
import java.util.List;

/** A small made-up restaurant for the rule tests. */
final class TestData {

    static final RestaurantFacts FACTS = new RestaurantFacts(
            "Burgo Smash Kitchen", "Shop 12, Cyber Hub Road, Gurugram", "+91 98765 43210",
            LocalTime.of(11, 0), LocalTime.of(23, 0), 40, 30, 5, 150, 40, 399,
            "Free two-wheeler parking at the front.");

    static final List<MenuEntry> MENU = List.of(
            new MenuEntry(1, "Classic Smash", "BURGERS", "Two smashed chicken patties with cheddar.", 199, false, 1, true, 540),
            new MenuEntry(2, "Paneer Tikka Crunch", "BURGERS", "Crispy paneer tikka patty with mint mayo.", 179, true, 2, true, 520),
            new MenuEntry(3, "Inferno Chicken", "BURGERS", "Fried chicken glazed in ghost pepper sauce.", 229, false, 3, false, 610),
            new MenuEntry(4, "Aloo Tikki Deluxe", "BURGERS", "Spiced potato patty with tamarind chutney.", 129, true, 1, false, 430),
            new MenuEntry(5, "Mushroom Melt", "BURGERS", "Garlic mushrooms under melted cheese.", 189, true, 0, false, 470),
            new MenuEntry(6, "Paneer Kathi Roll", "WRAPS", "Tandoori paneer in a flaky paratha.", 149, true, 1, true, 480),
            new MenuEntry(7, "Peri Peri Fries", "SIDES", "Fries tossed in peri peri seasoning.", 109, true, 2, true, 340),
            new MenuEntry(8, "Mango Lassi Shake", "DRINKS", "Alphonso mango, yogurt and cardamom.", 119, true, 0, true, 310),
            new MenuEntry(9, "Masala Lemonade", "DRINKS", "Fresh lime with roasted cumin.", 69, true, 0, false, 90),
            new MenuEntry(10, "Veggie Combo", "COMBOS", "Paneer Tikka Crunch, Peri Peri Fries and a drink.", 299, true, 2, true, 950));

    private TestData() {
    }
}
