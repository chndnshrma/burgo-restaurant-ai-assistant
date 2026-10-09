package com.burgo.api.service;

import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;

import com.burgo.api.core.MenuEntry;
import com.burgo.api.core.RestaurantFacts;
import com.burgo.api.domain.MenuItem;
import com.burgo.api.domain.Restaurant;
import com.burgo.api.web.Dto;

/** Small conversions between database rows, the framework-free core, and API shapes. */
final class Mappers {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final String REF_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I
    private static final SecureRandom RANDOM = new SecureRandom();

    private Mappers() {
    }

    static RestaurantFacts facts(Restaurant r) {
        return new RestaurantFacts(r.getName(), r.getAddress(), r.getPhone(), r.getOpens(), r.getCloses(),
                r.getSeatCapacity(), r.getSlotMinutes(), r.getDeliveryRadiusKm(), r.getMinDeliveryOrderInr(),
                r.getDeliveryFeeInr(), r.getFreeDeliveryAboveInr(), r.getParkingNote());
    }

    static MenuEntry entry(MenuItem m) {
        return new MenuEntry(m.getId(), m.getName(), m.getCategory().name(), m.getDescription(), m.getPriceInr(),
                m.isVeg(), m.getSpiceLevel(), m.isBestSeller(), m.getCalories());
    }

    static Dto.MenuItemDto dto(MenuItem m) {
        return new Dto.MenuItemDto(m.getId(), m.getName(), m.getDescription(), m.getCategory().name(),
                m.getPriceInr(), m.isVeg(), m.getSpiceLevel(), m.isBestSeller(), m.getCalories());
    }

    static Dto.RestaurantInfo info(Restaurant r) {
        return new Dto.RestaurantInfo(r.getSlug(), r.getName(), r.getTagline(), r.getAddress(), r.getPhone(),
                HH_MM.format(r.getOpens()), HH_MM.format(r.getCloses()), r.getSeatCapacity(),
                r.getDeliveryRadiusKm(), r.getDeliveryFeeInr(), r.getFreeDeliveryAboveInr(),
                r.getMinDeliveryOrderInr(), r.getParkingNote());
    }

    /** A short reference a customer can read out over the phone, like BG-7K3M9Q. */
    static String newReference(String prefix) {
        StringBuilder sb = new StringBuilder(prefix).append('-');
        for (int i = 0; i < 6; i++) {
            sb.append(REF_ALPHABET.charAt(RANDOM.nextInt(REF_ALPHABET.length())));
        }
        return sb.toString();
    }

    static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
