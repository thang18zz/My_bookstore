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
     * Ensure payment profile exists on MONA Pay.
     */
    public void ensurePaymentProfile() {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("display_name", "BookStore Vietnam");
            client()
                    .put()
                    .uri("/api/v1/payment-profile")
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .header("X-Client-Secret", config.getClientSecret())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            log.info("MONA Pay payment profile verified/created successfully");
        } catch (Exception e) {
            log.warn("Ensure payment profile warning: {}", e.getMessage());
        }
    }

    /**
     * Simulate Sandbox Transaction to trigger Mona Pay payment matcher.
     */
    public void createSandboxTransaction(Long amount, String orderCode, String virtualAccountNumber) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("amount", amount);
            body.put("description", orderCode);
            if (virtualAccountNumber != null && !virtualAccountNumber.isBlank()) {
                body.put("virtual_account_number", virtualAccountNumber);
            }
            client()
                    .post()
                    .uri("/api/v1/sandbox/transactions")
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .header("X-Client-Secret", config.getClientSecret())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            log.info("MONA Pay sandbox transaction submitted for {}", orderCode);
        } catch (Exception e) {
            log.warn("Mona Pay sandbox transaction simulation: {}", e.getMessage());
        }
    }

    private String sanitizeUrl(String url, String fallback) {
        if (url == null || url.isBlank() || url.contains("localhost") || url.contains("127.0.0.1")) {
            return fallback;
        }
        return url;
    }

    /**
     * Create MONA Pay Checkout.
     */
    public JsonNode createCheckout(
            Long amount,
            String orderCode,
            String description) {

        String returnUrl = sanitizeUrl(config.getReturnUrl(), "https://deafeningly-conjugated-marisha.ngrok-free.dev/orders");
        String cancelUrl = sanitizeUrl(config.getCancelUrl(), "https://deafeningly-conjugated-marisha.ngrok-free.dev/cart");

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);
        body.put("order_code", orderCode);
        body.put("description", description);
        body.put("return_url", returnUrl);
        body.put("cancel_url", cancelUrl);
        body.put("expires_in", config.getExpiresIn());
        body.put("sandbox", config.isSandbox());

        log.info("Creating MONA Pay checkout for orderCode={}, amount={}", orderCode, amount);

        try {
            return executeCreateCheckout(body, orderCode);
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            if (errorBody != null && errorBody.contains("payment_profile_missing")) {
                log.info("Payment profile missing on MONA Pay. Initializing payment profile and retrying...");
                ensurePaymentProfile();
                return executeCreateCheckout(body, orderCode);
            }
            log.error("MONA Pay create checkout failed: status={}, body={}", e.getStatusCode(), errorBody);
            throw new IllegalStateException("MONA Pay create checkout failed: " + errorBody, e);
        } catch (Exception e) {
            log.error("MONA Pay create checkout error: {}", e.getMessage());
            throw new IllegalStateException("MONA Pay create checkout error: " + e.getMessage(), e);
        }
    }

    private JsonNode executeCreateCheckout(Map<String, Object> body, String orderCode) {
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
