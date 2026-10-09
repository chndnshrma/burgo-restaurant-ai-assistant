package com.burgo.api.web;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.burgo.api.core.OrderType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request and response shapes of the public API, kept together so the contract is easy to read. */
public final class Dto {

    private Dto() {
    }

    private static final String PHONE = "^[+0-9][0-9 ()-]{6,19}$";

    // ---- menu

    public record RestaurantInfo(
            String slug, String name, String tagline, String address, String phone,
            String opens, String closes, int seatCapacity, int deliveryRadiusKm,
            int deliveryFeeInr, int freeDeliveryAboveInr, int minDeliveryOrderInr, String parkingNote) {
    }

    public record MenuItemDto(
            long id, String name, String description, String category, int priceInr,
            boolean veg, int spiceLevel, boolean bestSeller, int calories) {
    }

    public record MenuResponse(RestaurantInfo restaurant, List<MenuItemDto> items) {
    }

    // ---- reservations

    public record ReservationRequest(
            @NotBlank(message = "Please enter your name") @Size(max = 80) String name,
            @NotBlank(message = "Please enter a phone number")
            @Pattern(regexp = PHONE, message = "Enter a valid phone number") String phone,
            @NotNull(message = "Pick a date") LocalDate date,
            @NotNull(message = "Pick a time") LocalTime time,
            @Min(value = 1, message = "Tell us how many people are coming")
            @Max(value = 12, message = "For groups over 12, please call us") int partySize,
            @Size(max = 300, message = "Keep notes under 300 characters") String notes) {
    }

    public record ReservationResponse(
            String reference, String name, LocalDate date, LocalTime time, int partySize, String status) {
    }

    public record SlotDto(String time, int seatsLeft) {
    }

    public record SlotsResponse(LocalDate date, List<SlotDto> slots) {
    }

    // ---- orders

    public record OrderLineRequest(
            @NotNull(message = "Missing item") Long menuItemId,
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 20, message = "You can order up to 20 of each item") int quantity) {
    }

    public record OrderRequest(
            @NotNull(message = "Choose pickup or delivery") OrderType type,
            @NotBlank(message = "Please enter your name") @Size(max = 80) String name,
            @NotBlank(message = "Please enter a phone number")
            @Pattern(regexp = PHONE, message = "Enter a valid phone number") String phone,
            @Size(max = 300, message = "Keep the address under 300 characters") String address,
            @Size(max = 300, message = "Keep notes under 300 characters") String notes,
            @NotEmpty(message = "Your cart is empty") @Valid List<OrderLineRequest> items) {
    }

    public record OrderLineDto(String name, int quantity, int unitPriceInr, int lineTotalInr) {
    }

    public record OrderResponse(
            String reference, String status, OrderType type, List<OrderLineDto> items,
            int subtotalInr, int taxInr, int deliveryFeeInr, int totalInr, int etaMinutes, Instant createdAt) {
    }

    // ---- chat

    public record ChatRequest(@NotBlank(message = "Type a message") @Size(max = 500) String message) {
    }

    public record ChatResponse(String reply, String action, List<String> suggestions) {
    }

    // ---- errors

    public record ApiError(String message, Map<String, String> fieldErrors) {
    }
}
