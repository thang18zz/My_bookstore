package com.bookstore.chatbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        try {
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

            if (response != null && response.get("choices") instanceof List<?> list && !list.isEmpty()) {
                if (list.get(0) instanceof Map<?, ?> firstChoice && firstChoice.get("message") instanceof Map<?, ?> msgMap) {
                    Object content = msgMap.get("content");
                    if (content != null) {
                        return content.toString();
                    }
                }
            }
            return "Xin lỗi, chatbot đang gặp sự cố. Vui lòng thử lại sau hoặc yêu cầu kết nối nhân viên hỗ trợ.";
        } catch (Exception e) {
            log.error("Failed to communicate with Groq API: {}", e.getMessage(), e);
            return "Xin lỗi, hệ thống hỗ trợ tự động tạm thời gián đoạn (" + e.getMessage() + "). Quý khách có thể yêu cầu kết nối nhân viên hỗ trợ.";
        }
    }
}
