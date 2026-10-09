package com.burgo.api.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class OrderPricingTest {

    private static final RestaurantFacts F = TestData.FACTS;

    @Test
    void pickupAddsFivePercentGstRoundedToTheRupee() {
        var q = OrderPricing.quote(List.of(new OrderPricing.Line(199, 2)), OrderType.PICKUP, F);
        assertEquals(398, q.subtotalInr());
        assertEquals(20, q.taxInr()); // 5% of 398 is 19.9
        assertEquals(0, q.deliveryFeeInr());
        assertEquals(418, q.totalInr());
    }

    @Test
    void deliveryChargesAFeeUntilTheFreeThreshold() {
        var small = OrderPricing.quote(List.of(new OrderPricing.Line(179, 1)), OrderType.DELIVERY, F);
        assertEquals(40, small.deliveryFeeInr());
        var big = OrderPricing.quote(List.of(new OrderPricing.Line(200, 2)), OrderType.DELIVERY, F);
        assertEquals(0, big.deliveryFeeInr());
    }

    @Test
    void deliveryHasAMinimumButPickupDoesNot() {
        assertThrows(RuleViolation.class,
                () -> OrderPricing.quote(List.of(new OrderPricing.Line(129, 1)), OrderType.DELIVERY, F));
        assertDoesNotThrow(() -> OrderPricing.quote(List.of(new OrderPricing.Line(69, 1)), OrderType.PICKUP, F));
    }

    @Test
    void rejectsEmptyCartsAndSillyQuantities() {
        assertThrows(RuleViolation.class, () -> OrderPricing.quote(List.of(), OrderType.PICKUP, F));
        assertThrows(RuleViolation.class,
                () -> OrderPricing.quote(List.of(new OrderPricing.Line(100, 21)), OrderType.PICKUP, F));
        assertThrows(RuleViolation.class,
                () -> OrderPricing.quote(List.of(new OrderPricing.Line(100, 0)), OrderType.PICKUP, F));
    }

    @Test
    void deliveryEtaIsTwentyMinutesLongerThanPickup() {
        assertEquals(14, OrderPricing.etaMinutes(OrderType.PICKUP, 1));
        assertEquals(34, OrderPricing.etaMinutes(OrderType.DELIVERY, 1));
        assertEquals(44, OrderPricing.etaMinutes(OrderType.DELIVERY, 10)); // capped at 6 items of kitchen time
    }
}
