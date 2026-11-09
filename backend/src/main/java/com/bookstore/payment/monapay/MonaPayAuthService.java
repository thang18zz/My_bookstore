package com.bookstore.payment.monapay;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonaPayAuthService {

    private final MonaPayConfig config;

    private final WebClient.Builder webClientBuilder;

    private String accessToken;

    private Instant expiresAt;

    private final Object lock = new Object();

    public String getAccessToken() {

        synchronized (lock) {

            if (accessToken != null
                    && expiresAt != null
                    && Instant.now().isBefore(expiresAt.minusSeconds(60))) {

                return accessToken;
            }

            return requestNewToken();
        }
    }

    private String requestNewToken() {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("grant_type", "client_credentials");
        formData.add("client_id", config.getClientId());
        formData.add("client_secret", config.getClientSecret());

        WebClient client = webClientBuilder
                .baseUrl(config.getBaseUrl())
                .build();

        JsonNode response;
        try {
            response = client.post()
                    .uri("/api/v1/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("MONA Pay OAuth failed: status={}, response={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("MONA Pay authentication failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("MONA Pay OAuth request error: {}", e.getMessage());
            throw new IllegalStateException("MONA Pay authentication request failed: " + e.getMessage(), e);
        }

        if (response == null
                || !response.path("success").asBoolean(false)) {

            String msg = response != null ? response.path("message").asText("Unknown error") : "No response";
            log.error("MONA Pay OAuth authentication returned success=false: {}", msg);
            throw new IllegalStateException("MONA Pay authentication failed: " + msg);
        }

        JsonNode data = response.path("data");

        String token = data.path("access_token").asText(null);

        if (token == null || token.isBlank()) {

            throw new IllegalStateException(
                    "MONA Pay did not return access_token");
        }

        long expiresIn = data.path("expires_in").asLong(3600);

        this.accessToken = token;

        this.expiresAt = Instant.now().plusSeconds(expiresIn);

        log.info("MONA Pay access token acquired successfully, expires in {}s", expiresIn);

        return accessToken;
    }
}
