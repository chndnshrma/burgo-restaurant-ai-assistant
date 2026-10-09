package com.burgo.api.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The restaurant assistant's brain.
 *
 * <p>Everything it says comes from the restaurant's own data (facts + menu), so it cannot invent a price
 * or a dish. Common questions are routed by intent. Anything else goes through a small BM25 search over the
 * menu and the FAQ. If nothing matches well enough, it hands over to a human instead of guessing.
 *
 * <p>The same search is exposed through {@link #retrieve(String, int)} so an LLM can be given the best
 * matching facts as context when one is configured.
 */
public final class ChatEngine {

    public static final String ACTION_OPEN_RESERVATION = "OPEN_RESERVATION";
    public static final String ACTION_SCROLL_MENU = "SCROLL_MENU";

    /** {@code answered=false} means the engine had no good answer and fell back to a human handoff. */
    public record Reply(String text, String action, List<String> suggestions, boolean answered) {
    }

    private record Chunk(String text, MenuEntry item, Map<String, Integer> tf, int length) {
    }

    private static final double K1 = 1.5;
    private static final double B = 0.75;
    private static final double MIN_SCORE = 1.6;
    private static final int LIST_LIMIT = 6;

    private static final Set<String> STOPWORDS = Set.of(
            "a", "an", "the", "is", "are", "was", "were", "be", "do", "does", "did", "you", "your", "yours",
            "i", "me", "my", "we", "our", "us", "it", "its", "this", "that", "these", "those", "of", "to",
            "for", "in", "on", "at", "by", "with", "and", "or", "but", "if", "so", "than", "then", "can",
            "could", "would", "should", "will", "may", "please", "tell", "about", "what", "whats", "which",
            "who", "how", "any", "some", "have", "has", "had", "get", "got", "want", "need", "like", "serve",
            "give", "show", "there", "here", "also", "just", "very", "too", "much", "menu", "today");

    private final RestaurantFacts facts;
    private final List<MenuEntry> menu;
    private final List<Chunk> chunks = new ArrayList<>();
    private final Map<String, Integer> docFreq = new HashMap<>();
    private final double avgLength;

    public ChatEngine(RestaurantFacts facts, List<MenuEntry> menu) {
        this.facts = facts;
        this.menu = List.copyOf(menu);
        for (MenuEntry m : this.menu) {
            addChunk(describe(m), m);
        }
        for (String faq : faqs()) {
            addChunk(faq, null);
        }
        this.avgLength = chunks.stream().mapToInt(Chunk::length).average().orElse(1);
    }

    // ------------------------------------------------------------------ public API

    public Reply reply(String message) {
        String q = normalize(message == null ? "" : message).trim();
        if (q.isEmpty()) {
            return answer("Ask me about the menu, opening hours, delivery, or booking a table.", null);
        }
        Set<String> w = words(q);

        if (isGreeting(q, w)) {
            return answer("Hey! I'm the " + facts.name() + " assistant. I can help with the menu, delivery, "
                    + "opening hours and table bookings. What are you craving?", null);
        }
        if (has(w, "thanks", "thank", "thx", "thankyou") && w.size() <= 5) {
            return answer("Anytime! Anything else I can help with?", null);
        }
        if (has(w, "reserve", "reservation", "reservations", "booking", "bookings")
                || (has(w, "book") && has(w, "table", "tables", "seat", "seats"))
                || q.contains("table for")) {
            return answer("Happy to help you get a table. Tables can be booked every "
                    + facts.slotMinutes() + " minutes from " + ReservationRules.format(facts.opens()) + " to "
                    + ReservationRules.format(ReservationRules.lastSeating(facts)) + ", for up to "
                    + ReservationRules.MAX_PARTY + " guests. I've opened the booking form for you. "
                    + "For bigger groups, call " + facts.phone() + ".", ChatEngine.ACTION_OPEN_RESERVATION);
        }
        if (has(w, "status", "track") || q.contains("where is my order") || q.contains("where's my order")) {
            return answer("I can't see live order status from here. Call us on " + facts.phone()
                    + " with your order reference (it looks like ORD-ABC123) and the kitchen will tell you.", null);
        }
        if (has(w, "allergy", "allergies", "allergic", "allergen", "allergens", "gluten", "nut", "nuts",
                "peanut", "peanuts", "lactose", "dairy", "vegan", "jain", "halal", "diabetic", "celiac")) {
            return answer(allergy(), null);
        }
        if (has(w, "open", "opens", "opening", "closing", "close", "closes", "closed", "timing", "timings", "hours")) {
            return answer(hours(), null);
        }
        if (has(w, "address", "location", "directions", "located", "where", "parking", "park", "map")) {
            return answer("We're at " + facts.address() + ". " + facts.parkingNote(), null);
        }
        if (has(w, "deliver", "delivery", "takeaway", "takeout", "pickup", "dispatch")
                || q.contains("take away") || q.contains("order online")) {
            return answer(delivery(), ACTION_SCROLL_MENU);
        }
        if (has(w, "pay", "payment", "payments", "upi", "card", "cards", "cash", "gpay", "phonepe", "paytm")) {
            return answer(payment(), null);
        }

        MenuEntry named = findNamedItem(q);
        if (named != null) {
            return itemDetail(named);
        }

        boolean nonveg = has(w, "nonveg", "chicken", "egg", "eggs", "meat", "mutton", "fish");
        boolean veg = !nonveg && has(w, "veg", "vegetarian", "vegetarians", "veggie", "veggies");
        boolean hot = has(w, "spicy", "spicier", "hot", "fiery", "heat", "spice", "hottest");
        boolean mild = has(w, "mild");
        boolean best = has(w, "bestseller", "bestsellers", "popular", "recommend", "recommended",
                "recommendation", "favourite", "favorite", "favourites", "favorites", "suggest", "best");
        boolean cheapest = has(w, "cheap", "cheapest", "affordable", "budget");
        Integer maxPrice = parseBudget(q);
        String category = detectCategory(w);
        if (veg || nonveg || hot || mild || best || cheapest || maxPrice != null || category != null) {
            return menuQuery(veg, nonveg, hot, mild, best, cheapest, maxPrice, category);
        }

        if (has(w, "menu") || q.contains("what do you have") || q.contains("what do you serve")
                || q.contains("what all") || q.contains("what can i eat")) {
            return overview();
        }
        if (has(w, "order", "orders", "ordering")) {
            return answer(delivery(), ACTION_SCROLL_MENU);
        }

        // Long tail: search the menu and FAQ.
        List<String> qTokens = tokens(q);
        int bestIdx = -1;
        double bestScore = 0;
        for (int i = 0; i < chunks.size(); i++) {
            double s = score(chunks.get(i), qTokens);
            if (s > bestScore) {
                bestScore = s;
                bestIdx = i;
            }
        }
        if (bestIdx >= 0 && bestScore >= MIN_SCORE) {
            Chunk hit = chunks.get(bestIdx);
            return hit.item() != null ? itemDetail(hit.item()) : answer(hit.text(), null);
        }
        return new Reply("I'm not sure about that one, and I don't want to guess. Our team can help on "
                + facts.phone() + ", or ask me about the menu, opening hours, delivery, or booking a table.",
                null, defaults(), false);
    }

    /** The best matching facts for a question, most relevant first. Used as grounding for an LLM. */
    public List<String> retrieve(String message, int k) {
        List<String> qTokens = tokens(normalize(message == null ? "" : message));
        record Scored(Chunk chunk, double score) {
        }
        return chunks.stream()
                .map(c -> new Scored(c, score(c, qTokens)))
                .filter(x -> x.score() > 0)
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(k)
                .map(x -> x.chunk().text())
                .toList();
    }

    public static List<String> defaults() {
        return List.of("Show bestsellers", "Veg options", "Book a table", "Opening hours");
    }

    // ------------------------------------------------------------------ canned answers

    private String hours() {
        return "We're open every day, " + ReservationRules.format(facts.opens()) + " to "
                + ReservationRules.format(facts.closes()) + ". The last table is seated at "
                + ReservationRules.format(ReservationRules.lastSeating(facts)) + ".";
    }

    private String delivery() {
        return "We deliver within " + facts.deliveryRadiusKm() + " km. Delivery is ₹" + facts.deliveryFeeInr()
                + " and free on orders of ₹" + facts.freeDeliveryAboveInr() + " or more. Delivery orders start at ₹"
                + facts.minDeliveryOrderInr() + ". Pickup is usually ready in 15 to 25 minutes. "
                + "Add dishes to your cart from the menu and check out in a few taps.";
    }

    private String payment() {
        return "At the restaurant we take UPI, cards and cash. For online orders you pay when you pick up "
                + "or when it's delivered.";
    }

    private String allergy() {
        return "I can't confirm that any dish is allergen-free, vegan, jain or halal, and I won't guess about "
                + "something this important. Please tell our team about the allergy when you order, or call "
                + facts.phone() + ". On the menu I can tell you which dishes are veg and non-veg.";
    }

    private Reply overview() {
        int min = menu.stream().mapToInt(MenuEntry::priceInr).min().orElse(0);
        return answer("Our menu has burgers, wraps, sides, drinks and combos: " + menu.size()
                + " items in all, starting at ₹" + min + ". Ask me for veg, spicy or budget picks, "
                + "or scroll down to the full menu.", ACTION_SCROLL_MENU);
    }

    private Reply itemDetail(MenuEntry m) {
        String text = m.name() + " (" + label(m.category()) + ") is ₹" + m.priceInr() + ". "
                + m.description() + " " + (m.veg() ? "Veg" : "Non-veg") + ", " + m.spiceLabel()
                + ", about " + m.calories() + " kcal." + (m.bestSeller() ? " It's one of our best sellers." : "")
                + " You can add it from the menu.";
        return answer(text, ACTION_SCROLL_MENU);
    }

    private Reply menuQuery(boolean veg, boolean nonveg, boolean hot, boolean mild, boolean best,
                            boolean cheapest, Integer maxPrice, String category) {
        Stream<MenuEntry> s = menu.stream();
        if (veg) {
            s = s.filter(MenuEntry::veg);
        }
        if (nonveg) {
            s = s.filter(m -> !m.veg());
        }
        if (hot) {
            s = s.filter(m -> m.spiceLevel() >= 2);
        }
        if (mild) {
            s = s.filter(m -> m.spiceLevel() <= 1);
        }
        if (category != null) {
            s = s.filter(m -> m.category().equals(category));
        }
        if (maxPrice != null) {
            s = s.filter(m -> m.priceInr() <= maxPrice);
        }
        List<MenuEntry> list = new ArrayList<>(s.toList());
        if (best) {
            List<MenuEntry> sellers = list.stream().filter(MenuEntry::bestSeller).toList();
            if (!sellers.isEmpty()) {
                list = new ArrayList<>(sellers);
            }
        }

        Comparator<MenuEntry> order;
        if (cheapest) {
            order = Comparator.comparingInt(MenuEntry::priceInr);
        } else if (hot) {
            order = Comparator.comparingInt(MenuEntry::spiceLevel).reversed()
                    .thenComparing(Comparator.comparingInt(MenuEntry::priceInr));
        } else {
            order = Comparator.comparing(MenuEntry::bestSeller).reversed()
                    .thenComparing(Comparator.comparingInt(MenuEntry::priceInr));
        }
        list.sort(order);
        if (cheapest && maxPrice == null && list.size() > 3) {
            list = new ArrayList<>(list.subList(0, 3));
        }

        List<String> adjectives = new ArrayList<>();
        if (best) {
            adjectives.add("best-selling");
        }
        if (mild) {
            adjectives.add("mild");
        }
        if (hot) {
            adjectives.add("spicy");
        }
        if (veg) {
            adjectives.add("veg");
        }
        if (nonveg) {
            adjectives.add("non-veg");
        }
        String noun = category == null ? "items" : label(category).toLowerCase(Locale.ROOT);
        String desc = (adjectives.isEmpty() ? "" : String.join(" ", adjectives) + " ") + noun
                + (maxPrice != null ? " at ₹" + maxPrice + " or less" : "");

        if (list.isEmpty()) {
            return answer("I couldn't find any " + desc + " on the menu right now. Try loosening a filter, "
                    + "or ask for our bestsellers.", ACTION_SCROLL_MENU);
        }
        String intro = cheapest && maxPrice == null ? "Our cheapest " + desc + ":"
                : list.size() == 1 ? "There's 1 option, " + desc + ":" : "Here are " + list.size() + " " + desc + ":";
        return answer(intro + "\n" + bullets(list), ACTION_SCROLL_MENU);
    }

    private static String bullets(List<MenuEntry> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(LIST_LIMIT, items.size()); i++) {
            MenuEntry m = items.get(i);
            sb.append("• ").append(m.name()).append(" — ₹").append(m.priceInr())
                    .append(" (").append(m.veg() ? "veg" : "non-veg").append(", ").append(m.spiceLabel())
                    .append(")\n");
        }
        if (items.size() > LIST_LIMIT) {
            sb.append("…and ").append(items.size() - LIST_LIMIT).append(" more on the menu.");
        }
        return sb.toString().trim();
    }

    private static Reply answer(String text, String action) {
        return new Reply(text, action, defaults(), true);
    }

    // ------------------------------------------------------------------ knowledge base

    private String describe(MenuEntry m) {
        return m.name() + " (" + label(m.category()) + "): " + m.description() + " Price ₹" + m.priceInr() + ". "
                + (m.veg() ? "Vegetarian" : "Non-vegetarian") + ", " + m.spiceLabel() + ". "
                + m.calories() + " kcal." + (m.bestSeller() ? " Best seller." : "");
    }

    private List<String> faqs() {
        return List.of(
                "Opening hours: " + hours(),
                "Location and address: " + facts.address() + ". " + facts.parkingNote(),
                "Delivery and pickup: " + delivery(),
                "Payment: " + payment(),
                "Table reservations: you can book a table for up to " + ReservationRules.MAX_PARTY
                        + " guests, up to " + ReservationRules.MAX_DAYS_AHEAD + " days ahead.",
                "Allergies and dietary needs: " + allergy());
    }

    private void addChunk(String text, MenuEntry item) {
        List<String> toks = tokens(text);
        Map<String, Integer> tf = new HashMap<>();
        for (String t : toks) {
            tf.merge(t, 1, Integer::sum);
        }
        for (String t : tf.keySet()) {
            docFreq.merge(t, 1, Integer::sum);
        }
        chunks.add(new Chunk(text, item, tf, Math.max(1, toks.size())));
    }

    private double score(Chunk c, List<String> qTokens) {
        double total = 0;
        int n = chunks.size();
        for (String t : new LinkedHashSet<>(qTokens)) {
            int tf = c.tf().getOrDefault(t, 0);
            if (tf == 0) {
                continue;
            }
            int df = docFreq.getOrDefault(t, 0);
            double idf = Math.log(1 + (n - df + 0.5) / (df + 0.5));
            total += idf * (tf * (K1 + 1)) / (tf + K1 * (1 - B + B * c.length() / avgLength));
        }
        return total;
    }

    // ------------------------------------------------------------------ text helpers

    private static String normalize(String text) {
        String s = text.toLowerCase(Locale.ROOT);
        s = s.replaceAll("non[- ]?veg(etarian)?", "nonveg");
        s = s.replaceAll("\\b(not|less|non|no) (too )?spic(y|e)", "mild");
        return s;
    }

    private static Set<String> words(String q) {
        Set<String> out = new HashSet<>();
        for (String w : q.split("[^a-z0-9]+")) {
            if (!w.isEmpty()) {
                out.add(w);
            }
        }
        return out;
    }

    private static List<String> tokens(String text) {
        List<String> out = new ArrayList<>();
        for (String w : normalize(text).split("[^a-z0-9]+")) {
            if (w.length() > 1 && !STOPWORDS.contains(w)) {
                out.add(stem(w));
            }
        }
        return out;
    }

    private static String stem(String w) {
        if (w.length() > 4 && w.endsWith("ies")) {
            return w.substring(0, w.length() - 3) + "y";
        }
        if (w.length() > 3 && w.endsWith("s") && !w.endsWith("ss")) {
            return w.substring(0, w.length() - 1);
        }
        return w;
    }

    private static boolean has(Set<String> words, String... candidates) {
        return Arrays.stream(candidates).anyMatch(words::contains);
    }

    private static boolean isGreeting(String q, Set<String> w) {
        String first = q.split("[^a-z]+")[0];
        boolean opener = Set.of("hi", "hello", "hey", "hola", "namaste", "yo", "hii", "heyy").contains(first);
        return (opener && w.size() <= 3) || q.matches("^good (morning|afternoon|evening)\\W*$");
    }

    private static final Pattern BUDGET = Pattern.compile(
            "(?:under|below|within|upto|up to|less than|max|maximum|budget(?: of)?|around)\\s*(?:rs\\.?|inr|₹)?\\s*(\\d{2,4})"
                    + "|(?:rs\\.?|inr|₹)\\s*(\\d{2,4})");

    private static Integer parseBudget(String q) {
        Matcher m = BUDGET.matcher(q);
        if (m.find()) {
            return Integer.parseInt(m.group(1) != null ? m.group(1) : m.group(2));
        }
        return null;
    }

    private static String detectCategory(Set<String> w) {
        if (has(w, "combo", "combos", "meal", "meals", "deal", "deals", "offer", "offers")) {
            return "COMBOS";
        }
        if (has(w, "burger", "burgers")) {
            return "BURGERS";
        }
        if (has(w, "wrap", "wraps", "roll", "rolls", "kathi")) {
            return "WRAPS";
        }
        if (has(w, "fries", "fry", "sides", "side", "rings", "poppers", "snack", "snacks")) {
            return "SIDES";
        }
        if (has(w, "shake", "shakes", "drink", "drinks", "lemonade", "coffee", "lassi", "beverage", "beverages")) {
            return "DRINKS";
        }
        return null;
    }

    private MenuEntry findNamedItem(String q) {
        MenuEntry found = null;
        for (MenuEntry m : menu) {
            String name = m.name().toLowerCase(Locale.ROOT);
            if (q.contains(name) && (found == null || name.length() > found.name().length())) {
                found = m;
            }
        }
        return found;
    }

    private static String label(String category) {
        return switch (category) {
            case "BURGERS" -> "Burgers";
            case "WRAPS" -> "Wraps";
            case "SIDES" -> "Sides";
            case "DRINKS" -> "Drinks";
            case "COMBOS" -> "Combos";
            default -> category;
        };
    }
}
