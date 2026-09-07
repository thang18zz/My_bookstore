package com.bookstore.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Groq's OpenAI-compatible /chat/completions endpoint. Callers must already have
 * passed any DB-sourced text through DataSimplifier before it ends up in the "messages" list here -
 * this class does not know or care where the content came from, it just makes the HTTP call.
 */
@Component
@RequiredArgsConstructor
public class GroqClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.base-url}")
    private String baseUrl;

    @Value("${groq.model}")
    private String model;

    @Value("${groq.max-tokens}")
    private int maxTokens;

    @Value("${groq.temperature}")
    private double temperature;

    @SuppressWarnings("unchecked")
    public String chat(List<GroqMessage> messages) {
        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages.stream()
                        .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                        .toList(),
                "max_tokens", maxTokens,
                "temperature", temperature
        );

        Map<String, Object> response = client.post()
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("choices")) {
            return "Xin loi, chatbot dang gap su co. Vui long thu lai sau hoac yeu cau ket noi nhan vien ho tro.";
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}
