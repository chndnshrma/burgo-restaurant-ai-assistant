package com.burgo.api.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Optional long-tail answers from Google's Gemini API. Only used when GEMINI_API_KEY is set,
 * and only for questions the rule-based assistant could not answer from the menu or FAQ.
 * Any failure returns empty, so the chat never breaks because of it.
 */
@Component
class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    private final RestClient http;
    private final String apiKey;
    private final String model;

    GeminiClient(@Value("${burgo.gemini.api-key:}") String apiKey,
                 @Value("${burgo.gemini.model:gemini-3.8-flash}") String model) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(8));
        this.http = RestClient.builder().requestFactory(requestFactory).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        log.info("Gemini fallback {} (model {})", enabled() ? "ENABLED" : "disabled: GEMINI_API_KEY not set", model);
    }

    boolean enabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    Optional<String> generate(String systemPrompt, String userPrompt) {
        if (!enabled()) {
            return Optional.empty();
        }
        try {
            Map<String, Object> body = Map.of(
                    "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                    "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))),
                    // Gemini models may "think" before answering and that counts against maxOutputTokens, so the
                    // limit is generous enough to leave room for the short answer.
                    "generationConfig", Map.of("temperature", 0.2, "maxOutputTokens", 2048));
            JsonNode res = http.post()
                    .uri(ENDPOINT, model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            String text = res == null ? ""
                    : res.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
            if (text.isBlank()) {
                log.warn("Gemini returned no text (finishReason={}, promptFeedback={})",
                        res == null ? "n/a" : res.path("candidates").path(0).path("finishReason").asText("n/a"),
                        res == null ? "n/a" : res.path("promptFeedback").toString());
                return Optional.empty();
            }
            return Optional.of(text.trim());
        } catch (Exception e) {
            log.warn("Gemini request failed, using the built-in answer instead: {}", e.toString());
            return Optional.empty();
        }
    }
}