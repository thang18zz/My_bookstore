package com.bookstore.payment.monapay;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonaPayClient {

    private final MonaPayConfig config;

    private final MonaPayAuthService authService;

    private final WebClient.Builder webClientBuilder;

    private WebClient client() {

        return webClientBuilder
                .baseUrl(config.getBaseUrl())
                .build();
    }

    private WebClient.RequestHeadersSpec<?> authenticatedGet(
            String uri) {

        return client()
                .get()
                .uri(uri)
                .header(
                        "Authorization",
                        "Bearer " + authService.getAccessToken())
                .header(
                        "X-Client-Secret",
                        config.getClientSecret());
    }

    /**
     * Create MONA Pay Checkout.
     */
    public JsonNode createCheckout(
            Long amount,
            String orderCode,
            String description) {

        Map<String, Object> body = new HashMap<>();

        body.put("amount", amount);
        body.put("order_code", orderCode);
        body.put("description", description);

        body.put("return_url", config.getReturnUrl());
        body.put("cancel_url", config.getCancelUrl());

        body.put("expires_in", config.getExpiresIn());

        body.put("sandbox", config.isSandbox());

        log.info("Creating MONA Pay checkout for orderCode={}, amount={}", orderCode, amount);

        try {
            return client()
                    .post()
                    .uri("/api/v1/checkouts")
                    .header(
                            "Authorization",
                            "Bearer " + authService.getAccessToken())
                    .header(
                            "X-Client-Secret",
                            config.getClientSecret())
                    .header(
                            "Idempotency-Key",
                            "checkout-" + orderCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("MONA Pay create checkout failed: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("MONA Pay create checkout failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("MONA Pay create checkout error: {}", e.getMessage());
            throw new IllegalStateException("MONA Pay create checkout error: " + e.getMessage(), e);
        }
    }

    /**
     * Get Checkout information.
     */
    public JsonNode getCheckout(String checkoutId) {

        log.info("Querying MONA Pay checkout for checkoutId={}", checkoutId);

        try {
            return authenticatedGet(
                    "/api/v1/checkouts/" + checkoutId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("MONA Pay get checkout failed: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("MONA Pay get checkout failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("MONA Pay get checkout error: {}", e.getMessage());
            throw new IllegalStateException("MONA Pay get checkout error: " + e.getMessage(), e);
        }
    }

    /**
     * Extract data from MONA Pay response.
     */
    public JsonNode extractData(JsonNode response) {

        if (response == null) {
            throw new IllegalStateException(
                    "Empty response from MONA Pay");
        }

        if (!response.path("success").asBoolean(false)) {

            throw new IllegalStateException(
                    "MONA Pay API request failed: "
                            + response.path("message").asText("Unknown error"));
        }

        JsonNode data = response.path("data");

        if (data.isMissingNode() || data.isNull()) {

            throw new IllegalStateException(
                    "MONA Pay response does not contain data");
        }

        return data;
    }
}
