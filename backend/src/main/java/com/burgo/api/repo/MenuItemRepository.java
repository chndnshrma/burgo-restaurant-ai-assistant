package com.burgo.api.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.burgo.api.domain.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndIdIn(Long restaurantId, Collection<Long> ids);
}
