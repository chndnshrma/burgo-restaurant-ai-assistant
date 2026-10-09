package com.burgo.api.core;

import java.util.List;

/**
 * Turns cart lines into a bill. All money is whole rupees, so there is no rounding drift.
 * Prices come from the database, never from the browser.
 */
public final class OrderPricing {

    public static final int GST_PERCENT = 5;
    public static final int MAX_QTY_PER_LINE = 20;

    public record Line(int unitPriceInr, int quantity) {
    }

    public record Quote(int subtotalInr, int taxInr, int deliveryFeeInr, int totalInr) {
    }

    private OrderPricing() {
    }

    public static Quote quote(List<Line> lines, OrderType type, RestaurantFacts f) {
        if (lines == null || lines.isEmpty()) {
            throw new RuleViolation("Your cart is empty.");
        }
        int subtotal = 0;
        for (Line l : lines) {
            if (l.quantity() < 1 || l.quantity() > MAX_QTY_PER_LINE) {
                throw new RuleViolation("You can order between 1 and " + MAX_QTY_PER_LINE + " of each item.");
            }
            subtotal += l.unitPriceInr() * l.quantity();
        }
        int tax = (subtotal * GST_PERCENT + 50) / 100; // rounds half up
        int fee = 0;
        if (type == OrderType.DELIVERY) {
            if (subtotal < f.minDeliveryOrderInr()) {
                throw new RuleViolation("Delivery orders start at ₹" + f.minDeliveryOrderInr()
                        + ". Add a little more, or choose pickup.");
            }
            fee = subtotal >= f.freeDeliveryAboveInr() ? 0 : f.deliveryFeeInr();
        }
        return new Quote(subtotal, tax, fee, subtotal + tax + fee);
    }

    /** A rough promise for the customer, not a guarantee. */
    public static int etaMinutes(OrderType type, int itemCount) {
        int kitchen = 12 + 2 * Math.min(itemCount, 6);
        return type == OrderType.DELIVERY ? kitchen + 20 : kitchen;
    }
}
