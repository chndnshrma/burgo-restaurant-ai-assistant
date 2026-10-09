package com.burgo.api.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burgo.api.core.OrderPricing;
import com.burgo.api.core.OrderType;
import com.burgo.api.core.RestaurantFacts;
import com.burgo.api.core.RuleViolation;
import com.burgo.api.domain.CustomerOrder;
import com.burgo.api.domain.MenuItem;
import com.burgo.api.domain.OrderLine;
import com.burgo.api.domain.Restaurant;
import com.burgo.api.repo.MenuItemRepository;
import com.burgo.api.repo.OrderRepository;
import com.burgo.api.web.BusinessException;
import com.burgo.api.web.Dto;

@Service
public class OrderService {

    private final RestaurantFinder finder;
    private final MenuItemRepository menuItems;
    private final OrderRepository orders;

    OrderService(RestaurantFinder finder, MenuItemRepository menuItems, OrderRepository orders) {
        this.finder = finder;
        this.menuItems = menuItems;
        this.orders = orders;
    }

    @Transactional
    public Dto.OrderResponse place(String slug, Dto.OrderRequest req) {
        Restaurant r = finder.bySlug(slug);
        RestaurantFacts facts = Mappers.facts(r);

        if (req.type() == OrderType.DELIVERY && (req.address() == null || req.address().isBlank())) {
            throw new RuleViolation("Please add a delivery address.");
        }

        // Merge repeated items. The client sends ids and quantities only: prices are always read from the database.
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (Dto.OrderLineRequest line : req.items()) {
            quantities.merge(line.menuItemId(), line.quantity(), Integer::sum);
        }
        List<MenuItem> items = menuItems.findByRestaurantIdAndIdIn(r.getId(), quantities.keySet());
        if (items.size() != quantities.size() || items.stream().anyMatch(i -> !i.isAvailable())) {
            throw new RuleViolation("Some items in your cart are no longer available. Please refresh the menu.");
        }

        List<OrderPricing.Line> lines = items.stream()
                .map(i -> new OrderPricing.Line(i.getPriceInr(), quantities.get(i.getId())))
                .toList();
        OrderPricing.Quote quote = OrderPricing.quote(lines, req.type(), facts);
        int itemCount = quantities.values().stream().mapToInt(Integer::intValue).sum();

        CustomerOrder order = new CustomerOrder(Mappers.newReference("ORD"), r, req.type(), req.name().trim(),
                req.phone().trim(), req.type() == OrderType.DELIVERY ? Mappers.blankToNull(req.address()) : null,
                Mappers.blankToNull(req.notes()), quote.subtotalInr(), quote.taxInr(), quote.deliveryFeeInr(),
                quote.totalInr(), OrderPricing.etaMinutes(req.type(), itemCount));
        for (MenuItem item : items) {
            order.addLine(new OrderLine(item.getName(), item.getPriceInr(), quantities.get(item.getId())));
        }
        return toResponse(orders.save(order));
    }

    @Transactional(readOnly = true)
    public Dto.OrderResponse get(String slug, String reference) {
        Restaurant r = finder.bySlug(slug);
        return orders.findByReferenceAndRestaurantId(reference, r.getId())
                .map(OrderService::toResponse)
                .orElseThrow(() -> BusinessException.notFound("We couldn't find an order with that reference."));
    }

    private static Dto.OrderResponse toResponse(CustomerOrder o) {
        List<Dto.OrderLineDto> lines = o.getLines().stream()
                .map(l -> new Dto.OrderLineDto(l.getItemName(), l.getQuantity(), l.getUnitPriceInr(),
                        l.getUnitPriceInr() * l.getQuantity()))
                .toList();
        return new Dto.OrderResponse(o.getReference(), o.getStatus().name(), o.getType(), lines,
                o.getSubtotalInr(), o.getTaxInr(), o.getDeliveryFeeInr(), o.getTotalInr(),
                o.getEtaMinutes(), o.getCreatedAt());
    }
}
