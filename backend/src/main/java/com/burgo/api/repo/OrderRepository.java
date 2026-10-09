package com.burgo.api.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.burgo.api.domain.CustomerOrder;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = "lines")
    Optional<CustomerOrder> findByReferenceAndRestaurantId(String reference, Long restaurantId);
}
