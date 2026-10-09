package com.burgo.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChatEngineTest {

    private final ChatEngine engine = new ChatEngine(TestData.FACTS, TestData.MENU);

    @Test
    void filtersCombineVegCategoryAndBudget() {
        var r = engine.reply("Do you have veg burgers under 200?");
        assertTrue(r.answered());
        assertTrue(r.text().contains("Paneer Tikka Crunch"));
        assertTrue(r.text().contains("Aloo Tikki Deluxe"));
        assertFalse(r.text().contains("Classic Smash"), "chicken is not veg");
        assertFalse(r.text().contains("Inferno Chicken"));
    }

    @Test
    void nonVegNeverListsVegDishes() {
        var r = engine.reply("non veg options");
        assertTrue(r.text().contains("Classic Smash"));
        assertFalse(r.text().contains("Paneer"));
        assertFalse(r.text().contains("Mushroom"));
    }

    @Test
    void spicyListsTheHottestFirst() {
        String text = engine.reply("something spicy").text();
        assertTrue(text.indexOf("Inferno Chicken") < text.indexOf("Paneer Tikka Crunch"));
    }

    @Test
    void cheapestShowsTheLowestPricesFirst() {
        assertTrue(engine.reply("what is your cheapest item").text().contains("Masala Lemonade — ₹69"));
    }

    @Test
    void bookingOpensTheReservationForm() {
        var r = engine.reply("can I book a table for 4 tomorrow");
        assertEquals(ChatEngine.ACTION_OPEN_RESERVATION, r.action());
    }

    @Test
    void neverClaimsAFoodIsAllergenFree() {
        var r = engine.reply("is the mushroom melt gluten free?");
        assertTrue(r.answered());
        assertTrue(r.text().contains("can't confirm"));
        assertTrue(r.text().contains(TestData.FACTS.phone()));
    }

    @Test
    void answersHoursFromTheFacts() {
        assertTrue(engine.reply("What are your opening hours?").text().contains("11:00 AM to 11:00 PM"));
    }

    @Test
    void handsOverToAHumanInsteadOfGuessing() {
        for (String q : new String[] {"do you have pizza", "what's the weather like"}) {
            var r = engine.reply(q);
            assertFalse(r.answered(), q);
            assertTrue(r.text().contains(TestData.FACTS.phone()), q);
        }
    }

    @Test
    void retrievalFindsTheMatchingDish() {
        assertTrue(engine.retrieve("mango", 1).get(0).startsWith("Mango Lassi Shake"));
    }
}
