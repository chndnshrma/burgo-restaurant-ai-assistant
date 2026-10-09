package com.burgo.api.service;

import org.springframework.stereotype.Component;

import com.burgo.api.domain.Restaurant;
import com.burgo.api.repo.RestaurantRepository;
import com.burgo.api.web.BusinessException;

/** Looks a restaurant up by its URL slug, or answers 404. */
@Component
class RestaurantFinder {

    private final RestaurantRepository restaurants;

    RestaurantFinder(RestaurantRepository restaurants) {
        this.restaurants = restaurants;
    }

    Restaurant bySlug(String slug) {
        return restaurants.findBySlug(slug).orElseThrow(RestaurantFinder::missing);
    }

    Restaurant bySlugForUpdate(String slug) {
        return restaurants.findBySlugForUpdate(slug).orElseThrow(RestaurantFinder::missing);
    }

    private static BusinessException missing() {
        return BusinessException.notFound("We couldn't find that restaurant.");
    }
}
