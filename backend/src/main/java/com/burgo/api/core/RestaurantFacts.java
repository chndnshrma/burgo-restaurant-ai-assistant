package com.burgo.api.core;

import java.time.LocalTime;

/**
 * The facts about one restaurant that the business rules and the chatbot need.
 * Plain data, no framework types, so the rules stay easy to test.
 */
public record RestaurantFacts(
        String name,
        String address,
        String phone,
        LocalTime opens,
        LocalTime closes,
        int seatCapacity,
        int slotMinutes,
        int deliveryRadiusKm,
        int minDeliveryOrderInr,
        int deliveryFeeInr,
        int freeDeliveryAboveInr,
        String parkingNote) {
}
