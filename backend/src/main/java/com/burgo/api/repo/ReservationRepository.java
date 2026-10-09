package com.burgo.api.repo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.burgo.api.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** Seats already taken at one time slot. */
    @Query("""
            select coalesce(sum(r.partySize), 0L) from Reservation r
            where r.restaurant.id = :restaurantId and r.bookingDate = :date and r.bookingTime = :time
              and r.status = com.burgo.api.domain.ReservationStatus.CONFIRMED
            """)
    Long seatsTakenAt(@Param("restaurantId") Long restaurantId,
                      @Param("date") LocalDate date,
                      @Param("time") LocalTime time);

    /** Seats taken at every slot of a day: rows of [time, seats]. */
    @Query("""
            select r.bookingTime, sum(r.partySize) from Reservation r
            where r.restaurant.id = :restaurantId and r.bookingDate = :date
              and r.status = com.burgo.api.domain.ReservationStatus.CONFIRMED
            group by r.bookingTime
            """)
    List<Object[]> seatsTakenByTime(@Param("restaurantId") Long restaurantId, @Param("date") LocalDate date);
}
