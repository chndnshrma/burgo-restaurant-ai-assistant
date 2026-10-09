package com.burgo.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burgo.api.core.ChatEngine;
import com.burgo.api.core.MenuEntry;
import com.burgo.api.core.RestaurantFacts;
import com.burgo.api.domain.Restaurant;
import com.burgo.api.repo.MenuItemRepository;
import com.burgo.api.web.Dto;

@Service
public class ChatService {

    private final RestaurantFinder finder;
    private final MenuItemRepository menuItems;
    private final GeminiClient gemini;

    ChatService(RestaurantFinder finder, MenuItemRepository menuItems, GeminiClient gemini) {
        this.finder = finder;
        this.menuItems = menuItems;
        this.gemini = gemini;
    }

    @Transactional(readOnly = true)
    public Dto.ChatResponse reply(String slug, String message) {
        Restaurant r = finder.bySlug(slug);
        RestaurantFacts facts = Mappers.facts(r);
        List<MenuEntry> menu = menuItems.findByRestaurantIdAndAvailableTrue(r.getId()).stream()
                .map(Mappers::entry)
                .toList();

        ChatEngine engine = new ChatEngine(facts, menu);
        ChatEngine.Reply base = engine.reply(message);

        // The built-in engine answers anything it can from the restaurant's own data. Only when it has no good
        // answer, and a Gemini key is configured, the model gets a try, fed with the best matching facts.
        if (!base.answered() && gemini.enabled()) {
            List<String> context = engine.retrieve(message, 5);
            String system = "You are the friendly assistant for " + facts.name() + ", a burger restaurant. "
                    + "Answer ONLY from the facts provided. If the answer is not in the facts, say you're not sure "
                    + "and suggest calling " + facts.phone() + ". Never say a dish is allergen-free, vegan, jain "
                    + "or halal. Prices are in rupees (₹). Reply in at most three short sentences.";
            String user = "Facts:\n- " + String.join("\n- ", context) + "\n\nOpening hours: "
                    + facts.opens() + " to " + facts.closes() + ". Address: " + facts.address()
                    + ".\n\nCustomer question: " + message;
            var generated = gemini.generate(system, user);
            if (generated.isPresent()) {
                return new Dto.ChatResponse(generated.get(), base.action(), base.suggestions());
            }
        }
        return new Dto.ChatResponse(base.text(), base.action(), base.suggestions());
    }
}
