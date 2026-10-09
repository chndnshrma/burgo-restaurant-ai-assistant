package com.burgo.api.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.burgo.api.domain.Restaurant;

import jakarta.persistence.LockModeType;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findBySlug(String slug);

    /**
     * Takes a row lock on the restaurant. Two people booking the last seats at the same moment are
     * handled one after the other, so the table can't be overbooked.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Restaurant r where r.slug = :slug")
    Optional<Restaurant> findBySlugForUpdate(@Param("slug") String slug);
}
