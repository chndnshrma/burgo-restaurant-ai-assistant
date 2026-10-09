package com.burgo.api.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.burgo.api.service.ChatService;
import com.burgo.api.service.MenuService;
import com.burgo.api.service.OrderService;
import com.burgo.api.service.ReservationService;

import jakarta.validation.Valid;

/**
 * The whole public API for one restaurant, addressed by its slug, e.g. /api/r/burgo/menu.
 * Adding a second restaurant needs no code change: just another row.
 */
@RestController
@RequestMapping("/api/r/{slug}")
public class RestaurantController {

    private final MenuService menu;
    private final ReservationService reservations;
    private final OrderService orders;
    private final ChatService chat;

    public RestaurantController(MenuService menu, ReservationService reservations, OrderService orders,
                                ChatService chat) {
        this.menu = menu;
        this.reservations = reservations;
        this.orders = orders;
        this.chat = chat;
    }

    @GetMapping("/menu")
    public Dto.MenuResponse menu(@PathVariable String slug) {
        return menu.menu(slug);
    }

    @GetMapping("/reservations/slots")
    public Dto.SlotsResponse slots(@PathVariable String slug,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reservations.slots(slug, date);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public Dto.ReservationResponse reserve(@PathVariable String slug, @Valid @RequestBody Dto.ReservationRequest req) {
        return reservations.create(slug, req);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Dto.OrderResponse order(@PathVariable String slug, @Valid @RequestBody Dto.OrderRequest req) {
        return orders.place(slug, req);
    }

    @GetMapping("/orders/{reference}")
    public Dto.OrderResponse order(@PathVariable String slug, @PathVariable String reference) {
        return orders.get(slug, reference);
    }

    @PostMapping("/chat")
    public Dto.ChatResponse chat(@PathVariable String slug, @Valid @RequestBody Dto.ChatRequest req) {
        return chat.reply(slug, req.message());
    }
}
