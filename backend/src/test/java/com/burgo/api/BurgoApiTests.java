package com.burgo.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/** End-to-end checks through the real HTTP layer and an in-memory database seeded with the sample restaurant. */
@SpringBootTest
@AutoConfigureMockMvc
class BurgoApiTests {

    /** The restaurant's own time zone: the seeder and the booking rules both use it for "today". */
    private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");

    @Autowired
    MockMvc mvc;

    private String menuJson() throws Exception {
        return mvc.perform(get("/api/r/burgo/menu")).andReturn().getResponse().getContentAsString();
    }

    private long idOf(String itemName) throws Exception {
        List<Integer> ids = JsonPath.read(menuJson(), "$.items[?(@.name=='" + itemName + "')].id");
        return ids.get(0);
    }

    private static String reservation(LocalDate date, String time, int party) {
        return """
                {"name":"Test Guest","phone":"+91 98765 00000","date":"%s","time":"%s","partySize":%d}
                """.formatted(date, time, party);
    }

    @Test
    void menuIsSeededAndSorted() throws Exception {
        mvc.perform(get("/api/r/burgo/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurant.name").value("Burgo Smash Kitchen"))
                .andExpect(jsonPath("$.items.length()", greaterThan(15)))
                .andExpect(jsonPath("$.items[0].category").value("BURGERS"));
    }

    @Test
    void unknownRestaurantIs404() throws Exception {
        mvc.perform(get("/api/r/nope/menu")).andExpect(status().isNotFound());
    }

    @Test
    void slotsShowSeatsLeftAfterTheSeededBookings() throws Exception {
        LocalDate tomorrow = LocalDate.now(KOLKATA).plusDays(1);
        String body = mvc.perform(get("/api/r/burgo/reservations/slots").param("date", tomorrow.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Integer> left = JsonPath.read(body, "$.slots[?(@.time=='19:30')].seatsLeft");
        assertEquals(4, left.get(0)); // 3 seeded parties of 12 leave 4 of 40 seats
    }

    @Test
    void bookingSucceedsThenFillsUp() throws Exception {
        LocalDate day = LocalDate.now(KOLKATA).plusDays(3);
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/r/burgo/reservations").contentType(MediaType.APPLICATION_JSON)
                            .content(reservation(day, "20:00", 12)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reference", containsString("BG-")));
        }
        // 36 of 40 seats are taken, so 5 more can't fit but 4 can.
        mvc.perform(post("/api/r/burgo/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(reservation(day, "20:00", 5)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("4 seats")));
        mvc.perform(post("/api/r/burgo/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(reservation(day, "20:00", 4)))
                .andExpect(status().isCreated());
    }

    @Test
    void bookingOutsideOpeningHoursIsRejected() throws Exception {
        mvc.perform(post("/api/r/burgo/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(reservation(LocalDate.now(KOLKATA).plusDays(2), "03:00", 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Pick a time between")));
    }

    @Test
    void bookingWithMissingFieldsReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/r/burgo/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());
    }

    @Test
    void orderTotalsAreComputedOnTheServer() throws Exception {
        long id = idOf("Classic Smash");
        String body = """
                {"type":"PICKUP","name":"Asha","phone":"+91 98765 11111","items":[{"menuItemId":%d,"quantity":2}]}
                """.formatted(id);
        mvc.perform(post("/api/r/burgo/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotalInr").value(398))
                .andExpect(jsonPath("$.taxInr").value(20))
                .andExpect(jsonPath("$.totalInr").value(418))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.reference", containsString("ORD-")));
    }

    @Test
    void deliveryNeedsAnAddressAndAMinimumOrder() throws Exception {
        long cheap = idOf("Aloo Tikki Deluxe"); // 129, below the 150 delivery minimum
        mvc.perform(post("/api/r/burgo/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type":"DELIVERY","name":"Asha","phone":"+91 98765 11111","items":[{"menuItemId":%d,"quantity":1}]}
                        """.formatted(cheap)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("delivery address")));
        mvc.perform(post("/api/r/burgo/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type":"DELIVERY","name":"Asha","phone":"+91 98765 11111","address":"12 MG Road",
                         "items":[{"menuItemId":%d,"quantity":1}]}
                        """.formatted(cheap)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("start at")));
    }

    @Test
    void orderCanBeLookedUpByReference() throws Exception {
        long id = idOf("Masala Lemonade");
        String created = mvc.perform(post("/api/r/burgo/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type":"PICKUP","name":"Ravi","phone":"+91 98765 22222","items":[{"menuItemId":%d,"quantity":1}]}
                        """.formatted(id)))
                .andReturn().getResponse().getContentAsString();
        String ref = JsonPath.read(created, "$.reference");
        mvc.perform(get("/api/r/burgo/orders/" + ref))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Masala Lemonade"));
    }

    @Test
    void chatAnswersFromTheMenu() throws Exception {
        mvc.perform(post("/api/r/burgo/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Do you have veg burgers under 200?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply", containsString("Paneer Tikka Crunch")))
                .andExpect(jsonPath("$.action").value("SCROLL_MENU"));
    }
}
