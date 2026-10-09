package com.burgo.api.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burgo.api.domain.MenuItem;
import com.burgo.api.domain.Restaurant;
import com.burgo.api.repo.MenuItemRepository;
import com.burgo.api.web.Dto;

@Service
public class MenuService {

    private final RestaurantFinder finder;
    private final MenuItemRepository menuItems;

    MenuService(RestaurantFinder finder, MenuItemRepository menuItems) {
        this.finder = finder;
        this.menuItems = menuItems;
    }

    @Transactional(readOnly = true)
    public Dto.MenuResponse menu(String slug) {
        Restaurant r = finder.bySlug(slug);
        List<Dto.MenuItemDto> items = menuItems.findByRestaurantIdAndAvailableTrue(r.getId()).stream()
                .sorted(Comparator.comparing(MenuItem::getCategory)
                        .thenComparing(MenuItem::isBestSeller, Comparator.reverseOrder())
                        .thenComparingInt(MenuItem::getPriceInr))
                .map(Mappers::dto)
                .toList();
        return new Dto.MenuResponse(Mappers.info(r), items);
    }
}
