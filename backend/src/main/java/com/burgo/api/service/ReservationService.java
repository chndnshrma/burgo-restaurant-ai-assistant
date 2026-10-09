package com.burgo.api.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burgo.api.core.ReservationRules;
import com.burgo.api.core.RestaurantFacts;
import com.burgo.api.domain.Reservation;
import com.burgo.api.domain.Restaurant;
import com.burgo.api.repo.ReservationRepository;
import com.burgo.api.web.BusinessException;
import com.burgo.api.web.Dto;

@Service
public class ReservationService {

    private final RestaurantFinder finder;
    private final ReservationRepository reservations;
    private final Clock clock;

    ReservationService(RestaurantFinder finder, ReservationRepository reservations, Clock clock) {
        this.finder = finder;
        this.reservations = reservations;
        this.clock = clock;
    }

    /** Which times are open on a day, and how many seats are left at each. */
    @Transactional(readOnly = true)
    public Dto.SlotsResponse slots(String slug, LocalDate date) {
        Restaurant r = finder.bySlug(slug);
        RestaurantFacts facts = Mappers.facts(r);
        LocalDateTime now = LocalDateTime.now(clock);

        Map<LocalTime, Integer> taken = new HashMap<>();
        for (Object[] row : reservations.seatsTakenByTime(r.getId(), date)) {
            taken.put(((LocalTime) row[0]).truncatedTo(ChronoUnit.MINUTES), ((Number) row[1]).intValue());
        }

        List<Dto.SlotDto> slots = ReservationRules.slots(facts).stream()
                .map(t -> {
                    boolean tooSoon = LocalDateTime.of(date, t)
                            .isBefore(now.plusMinutes(ReservationRules.MIN_LEAD_MINUTES));
                    int left = tooSoon ? 0
                            : ReservationRules.seatsLeft(taken.getOrDefault(t, 0), facts.seatCapacity());
                    return new Dto.SlotDto(t.toString(), left);
                })
                .toList();
        return new Dto.SlotsResponse(date, slots);
    }

    @Transactional
    public Dto.ReservationResponse create(String slug, Dto.ReservationRequest req) {
        // Locks the restaurant row until this transaction ends, so seats can't be double-sold.
        Restaurant r = finder.bySlugForUpdate(slug);
        RestaurantFacts facts = Mappers.facts(r);
        LocalTime time = req.time().truncatedTo(ChronoUnit.MINUTES);

        ReservationRules.validate(req.date(), time, req.partySize(), LocalDateTime.now(clock), facts);

        int taken = reservations.seatsTakenAt(r.getId(), req.date(), time).intValue();
        if (!ReservationRules.hasCapacity(taken, req.partySize(), facts.seatCapacity())) {
            int left = ReservationRules.seatsLeft(taken, facts.seatCapacity());
            throw new BusinessException(HttpStatus.CONFLICT, left == 0
                    ? "That time is fully booked. Please pick another slot."
                    : "Only " + left + " seats are left at that time. Try a smaller group or another slot.");
        }

        Reservation saved = reservations.save(new Reservation(
                Mappers.newReference("BG"), r, req.name().trim(), req.phone().trim(),
                req.date(), time, req.partySize(), Mappers.blankToNull(req.notes())));

        return new Dto.ReservationResponse(saved.getReference(), saved.getGuestName(), saved.getBookingDate(),
                saved.getBookingTime(), saved.getPartySize(), saved.getStatus().name());
    }
}
