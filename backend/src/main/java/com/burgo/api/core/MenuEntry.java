package com.burgo.api.core;

/** One dish, as the chatbot sees it. Prices are whole rupees. */
public record MenuEntry(
        long id,
        String name,
        String category,
        String description,
        int priceInr,
        boolean veg,
        int spiceLevel,
        boolean bestSeller,
        int calories) {

    public String spiceLabel() {
        return switch (spiceLevel) {
            case 0 -> "not spicy";
            case 1 -> "mild";
            case 2 -> "spicy";
            default -> "very spicy";
        };
    }
}
