package com.burgo.api.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.burgo.api.domain.MenuCategory;
import com.burgo.api.domain.MenuItem;
import com.burgo.api.domain.Reservation;
import com.burgo.api.domain.Restaurant;
import com.burgo.api.repo.MenuItemRepository;
import com.burgo.api.repo.ReservationRepository;
import com.burgo.api.repo.RestaurantRepository;

import static com.burgo.api.domain.MenuCategory.BURGERS;
import static com.burgo.api.domain.MenuCategory.COMBOS;
import static com.burgo.api.domain.MenuCategory.DRINKS;
import static com.burgo.api.domain.MenuCategory.SIDES;
import static com.burgo.api.domain.MenuCategory.WRAPS;

/** Loads a sample restaurant the first time the app starts on an empty database. All data here is made up. */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RestaurantRepository restaurants;
    private final MenuItemRepository menuItems;
    private final ReservationRepository reservations;
    private final Clock clock;

    public DataSeeder(RestaurantRepository restaurants, MenuItemRepository menuItems,
                      ReservationRepository reservations, Clock clock) {
        this.restaurants = restaurants;
        this.menuItems = menuItems;
        this.reservations = reservations;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (restaurants.count() > 0) {
            return;
        }
        Restaurant r = restaurants.save(new Restaurant(
                "burgo", "Burgo Smash Kitchen", "Smashed fresh. Stacked loud.",
                "Shop 12, Cyber Hub Road, Gurugram, Haryana 122002", "+91 98765 43210",
                LocalTime.of(11, 0), LocalTime.of(23, 0), 40, 30, 5, 150, 40, 399,
                "Free two-wheeler parking at the front; car parking in the building basement."));

        menuItems.saveAll(List.of(
                // Burgers
                item(r, "Classic Smash", "Two smashed chicken patties, melted cheddar, pickles and our burger sauce on a toasted bun.", BURGERS, 199, false, 1, true, 540),
                item(r, "Paneer Tikka Crunch", "Crispy paneer tikka patty with mint mayo, onion rings and pickled onions.", BURGERS, 179, true, 2, true, 520),
                item(r, "Inferno Chicken", "Fried chicken thigh glazed in ghost pepper sauce, with cool slaw to take the edge off.", BURGERS, 229, false, 3, false, 610),
                item(r, "Aloo Tikki Deluxe", "Spiced potato patty, tamarind chutney, onions and a cheese slice.", BURGERS, 129, true, 1, false, 430),
                item(r, "Mushroom Melt", "Garlic mushrooms under a blanket of melted cheese, with caramelised onions.", BURGERS, 189, true, 0, false, 470),
                item(r, "Egg Bomb", "Chicken patty topped with a fried egg and pickled jalapeños.", BURGERS, 219, false, 2, false, 580),
                // Wraps
                item(r, "Peri Peri Chicken Wrap", "Grilled peri peri chicken, lettuce and garlic mayo in a warm wrap.", WRAPS, 169, false, 2, false, 450),
                item(r, "Paneer Kathi Roll", "Tandoori paneer, onions and green chutney rolled in a flaky paratha.", WRAPS, 149, true, 1, true, 480),
                item(r, "Falafel Hummus Wrap", "Crisp falafel, creamy hummus and crunchy vegetables.", WRAPS, 159, true, 0, false, 420),
                // Sides
                item(r, "Classic Salted Fries", "Skin-on fries with sea salt.", SIDES, 89, true, 0, false, 320),
                item(r, "Peri Peri Fries", "Fries tossed in peri peri seasoning.", SIDES, 109, true, 2, true, 340),
                item(r, "Masala Cheese Fries", "Fries with chaat masala and a warm cheese sauce.", SIDES, 129, true, 1, false, 410),
                item(r, "Onion Rings", "Thick-cut onion rings in a crunchy batter.", SIDES, 99, true, 0, false, 300),
                item(r, "Crispy Chicken Poppers", "Bite-sized fried chicken with a garlic dip.", SIDES, 139, false, 1, false, 380),
                // Drinks
                item(r, "Thick Chocolate Shake", "Ice cream blended with dark chocolate.", DRINKS, 139, true, 0, false, 480),
                item(r, "Mango Lassi Shake", "Alphonso mango, yogurt and a pinch of cardamom.", DRINKS, 119, true, 0, true, 310),
                item(r, "Oreo Crumble Shake", "Vanilla ice cream blended with cookie crumble.", DRINKS, 149, true, 0, false, 520),
                item(r, "Masala Lemonade", "Fresh lime with roasted cumin and black salt.", DRINKS, 69, true, 0, false, 90),
                item(r, "Cold Coffee", "Chilled coffee blended with milk and a scoop of ice cream.", DRINKS, 109, true, 0, false, 260),
                // Combos
                item(r, "Smash Combo", "Classic Smash, salted fries and any drink.", COMBOS, 329, false, 1, false, 980),
                item(r, "Veggie Combo", "Paneer Tikka Crunch, Peri Peri Fries and a Masala Lemonade.", COMBOS, 299, true, 2, true, 950),
                item(r, "Squad Box", "Four burgers, two fries and four drinks to share.", COMBOS, 799, false, 1, false, 3200)));

        // Three big parties already booked tomorrow at 7:30 pm, so you can see the capacity rules in action.
        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);
        for (int i = 1; i <= 3; i++) {
            reservations.save(new Reservation("BG-SAMPL" + i, r, "Sample party " + i, "+91 90000 00000",
                    tomorrow, LocalTime.of(19, 30), 12, null));
        }
        log.info("Seeded sample restaurant '{}' with {} menu items", r.getSlug(), menuItems.count());
    }

    private static MenuItem item(Restaurant r, String name, String description, MenuCategory category,
                                 int priceInr, boolean veg, int spice, boolean bestSeller, int calories) {
        return new MenuItem(r, name, description, category, priceInr, veg, spice, bestSeller, calories);
    }
}
