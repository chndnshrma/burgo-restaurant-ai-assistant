package com.burgo.api.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class ReservationRulesTest {

    private static final RestaurantFacts F = TestData.FACTS;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 8, 16, 45);
    private static final LocalDate TOMORROW = NOW.toLocalDate().plusDays(1);

    @Test
    void slotsRunFromOpeningToLastSeating() {
        List<LocalTime> slots = ReservationRules.slots(F);
        assertEquals(LocalTime.of(11, 0), slots.get(0));
        assertEquals(LocalTime.of(22, 0), slots.get(slots.size() - 1));
        assertEquals(23, slots.size());
    }

    @Test
    void acceptsAGoodBooking() {
        assertDoesNotThrow(() -> ReservationRules.validate(TOMORROW, LocalTime.of(19, 30), 4, NOW, F));
    }

    @Test
    void rejectsBadBookings() {
        assertThrows(RuleViolation.class, () -> ReservationRules.validate(TOMORROW, LocalTime.of(3, 0), 2, NOW, F));
        assertThrows(RuleViolation.class, () -> ReservationRules.validate(TOMORROW, LocalTime.of(22, 30), 2, NOW, F));
        assertThrows(RuleViolation.class, () -> ReservationRules.validate(TOMORROW, LocalTime.of(19, 15), 2, NOW, F));
        assertThrows(RuleViolation.class, () -> ReservationRules.validate(TOMORROW, LocalTime.of(19, 30), 13, NOW, F));
        assertThrows(RuleViolation.class, () -> ReservationRules.validate(TOMORROW, LocalTime.of(19, 30), 0, NOW, F));
        assertThrows(RuleViolation.class,
                () -> ReservationRules.validate(NOW.toLocalDate().minusDays(1), LocalTime.of(19, 30), 2, NOW, F));
        assertThrows(RuleViolation.class,
                () -> ReservationRules.validate(NOW.toLocalDate().plusDays(31), LocalTime.of(19, 30), 2, NOW, F));
    }

    @Test
    void needsThirtyMinutesNotice() {
        assertThrows(RuleViolation.class,
                () -> ReservationRules.validate(NOW.toLocalDate(), LocalTime.of(17, 0), 2, NOW, F));
        assertDoesNotThrow(() -> ReservationRules.validate(NOW.toLocalDate(), LocalTime.of(17, 30), 2, NOW, F));
    }

    @Test
    void capacityIsCheckedAgainstSeatsAlreadyTaken() {
        assertTrue(ReservationRules.hasCapacity(36, 4, 40));
        assertFalse(ReservationRules.hasCapacity(36, 5, 40));
        assertEquals(0, ReservationRules.seatsLeft(50, 40));
        assertEquals(4, ReservationRules.seatsLeft(36, 40));
    }
}
