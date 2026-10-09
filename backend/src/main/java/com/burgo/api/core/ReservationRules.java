package com.burgo.api.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Who can book which table, and when. Pure logic: give it facts, get a verdict. */
public final class ReservationRules {

    public static final int MAX_PARTY = 12;
    public static final int MAX_DAYS_AHEAD = 30;
    public static final int MIN_LEAD_MINUTES = 30;
    /** The last table is seated this long before closing. */
    public static final int DINING_MINUTES = 60;

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private ReservationRules() {
    }

    /** Every bookable start time of the day, from opening to the last seating. */
    public static List<LocalTime> slots(RestaurantFacts f) {
        List<LocalTime> out = new ArrayList<>();
        LocalTime last = f.closes().minusMinutes(DINING_MINUTES);
        LocalTime t = f.opens();
        while (!t.isAfter(last)) {
            out.add(t);
            LocalTime next = t.plusMinutes(f.slotMinutes());
            if (!next.isAfter(t)) {
                break; // wrapped past midnight
            }
            t = next;
        }
        return out;
    }

    public static LocalTime lastSeating(RestaurantFacts f) {
        return f.closes().minusMinutes(DINING_MINUTES);
    }

    /** Throws {@link RuleViolation} with a customer-ready message if the request can't be accepted. */
    public static void validate(LocalDate date, LocalTime time, int partySize, LocalDateTime now, RestaurantFacts f) {
        if (partySize < 1) {
            throw new RuleViolation("Please tell us how many people are coming.");
        }
        if (partySize > MAX_PARTY) {
            throw new RuleViolation("For groups of more than " + MAX_PARTY + ", please call us on " + f.phone() + ".");
        }
        if (date.isBefore(now.toLocalDate())) {
            throw new RuleViolation("That date has already passed.");
        }
        if (date.isAfter(now.toLocalDate().plusDays(MAX_DAYS_AHEAD))) {
            throw new RuleViolation("We take bookings up to " + MAX_DAYS_AHEAD + " days ahead.");
        }
        LocalTime clean = time.truncatedTo(ChronoUnit.MINUTES);
        if (!slots(f).contains(clean)) {
            throw new RuleViolation("Pick a time between " + format(f.opens()) + " and "
                    + format(lastSeating(f)) + ", on the hour or half hour.");
        }
        if (LocalDateTime.of(date, clean).isBefore(now.plusMinutes(MIN_LEAD_MINUTES))) {
            throw new RuleViolation("Bookings need at least " + MIN_LEAD_MINUTES + " minutes' notice.");
        }
    }

    public static int seatsLeft(int alreadyBooked, int capacity) {
        return Math.max(0, capacity - alreadyBooked);
    }

    public static boolean hasCapacity(int alreadyBooked, int partySize, int capacity) {
        return alreadyBooked + partySize <= capacity;
    }

    public static String format(LocalTime t) {
        return CLOCK.format(t);
    }
}
