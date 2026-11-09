package com.bookstore.payment.monapay;

import com.bookstore.order.Order;
import com.bookstore.order.OrderRepository;
import com.bookstore.order.OrderStatus;
import com.bookstore.order.PaymentStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonaPayWebhookService {

    private final MonaPayConfig config;

    private final MonaPayClient monaPayClient;

    private final OrderRepository orderRepository;

    private final ObjectMapper objectMapper;

    /**
     * Main webhook handler.
     */
    @Transactional
    public void handleWebhook(
            byte[] rawBody,
            String timestamp,
            String signature) {

        // 1. Verify HMAC.
        verifySignature(
                rawBody,
                timestamp,
                signature);

        // 2. Parse JSON after signature verification.
        JsonNode payload;

        try {
            payload = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            log.error("Invalid webhook JSON: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid webhook JSON");
        }

        // 3. Read event.
        String event = payload.path("event")
                .asText(
                        payload.path("event_type").asText(""));

        log.info("Received MonaPay webhook event: '{}'", event);

        /*
         * If event is not CHECKOUT_PAID (e.g. test dummy webhook or other notifications),
         * acknowledge receipt successfully (HTTP 200).
         */
        if (!"CHECKOUT_PAID".equalsIgnoreCase(event)) {
            log.info("MonaPay event '{}' is not CHECKOUT_PAID. Acknowledged OK.", event);
            return;
        }

        // Support both flat payload and nested 'data' payload
        JsonNode dataNode = (payload.has("data") && payload.get("data").isObject())
                ? payload.get("data")
                : payload;

        String checkoutId = dataNode.path("checkout_id").asText(payload.path("checkout_id").asText(""));
        String orderCode = dataNode.path("order_code").asText(payload.path("order_code").asText(""));
        String transactionCode = dataNode.path("transaction_code").asText(payload.path("transaction_code").asText(""));

        BigDecimal amount = dataNode.hasNonNull("amount")
                ? dataNode.get("amount").decimalValue()
                : (payload.hasNonNull("amount") ? payload.get("amount").decimalValue() : null);

        BigDecimal paidAmount = dataNode.hasNonNull("paid_amount")
                ? dataNode.get("paid_amount").decimalValue()
                : (payload.hasNonNull("paid_amount") ? payload.get("paid_amount").decimalValue() : amount);

        if (checkoutId.isBlank()
                || orderCode.isBlank()
                || transactionCode.isBlank()
                || amount == null
                || paidAmount == null) {

            log.warn("Missing required payment information in MonaPay webhook: checkoutId={}, orderCode={}, transactionCode={}, amount={}, paidAmount={}",
                    checkoutId, orderCode, transactionCode, amount, paidAmount);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing payment information");
        }

        // 4. Find order by MONA Pay Checkout ID.
        Order order = orderRepository
                .findByPaymentReference(checkoutId)
                .orElseThrow(() -> {
                    log.warn("Order not found for checkoutId: {}", checkoutId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Order not found");
                });

        // 5. Verify order code.
        String expectedOrderCode = "ORDER-" + order.getId();

        if (!expectedOrderCode.equals(orderCode)) {
            log.warn("Order code mismatch: expected '{}', got '{}'", expectedOrderCode, orderCode);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order code mismatch");
        }

        // 6. Verify amount.
        if (amount.compareTo(order.getTotalAmount()) != 0
                || paidAmount.compareTo(order.getTotalAmount()) < 0) {
            log.warn("Payment amount mismatch: order amount={}, webhook amount={}, paidAmount={}",
                    order.getTotalAmount(), amount, paidAmount);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment amount mismatch");
        }

        /*
         * 7. Query MONA Pay again.
         * Never trust the webhook alone for final payment confirmation.
         */
        JsonNode checkoutResponse = monaPayClient.getCheckout(checkoutId);
        JsonNode checkout = monaPayClient.extractData(checkoutResponse);

        String checkoutStatus = checkout.path("status").asText();

        if (!"paid".equalsIgnoreCase(checkoutStatus)) {
            log.warn("MONA Pay checkout '{}' is not paid, status: {}", checkoutId, checkoutStatus);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "MONA Pay checkout is not paid");
        }

        // Verify the returned Checkout information.
        if (!orderCode.equals(checkout.path("order_code").asText())
                || checkout.path("amount").asLong() != order.getTotalAmount().longValue()) {

            log.warn("MONA Pay checkout information mismatch from API verification: orderCode={}, amount={}",
                    checkout.path("order_code").asText(), checkout.path("amount").asText());

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "MONA Pay checkout information mismatch");
        }

        // 8. Idempotency: ignore duplicate webhook.
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Order #{} is already PAID. Skipping duplicate processing.", order.getId());
            return;
        }

        // 9. Update order.
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setTransactionId(transactionCode);

        String paidAt = checkout.path("paid_at")
                .asText(
                        dataNode.path("paid_at").asText(payload.path("paid_at").asText("")));

        if (!paidAt.isBlank()) {
            try {
                order.setPaidAt(
                        OffsetDateTime.parse(paidAt)
                                .atZoneSameInstant(ZoneId.systemDefault())
                                .toLocalDateTime());
            } catch (Exception e) {
                log.warn("Failed to parse paidAt '{}', falling back to current time", paidAt);
                order.setPaidAt(LocalDateTime.now());
            }
        } else {
            order.setPaidAt(LocalDateTime.now());
        }

        orderRepository.save(order);
        log.info("Payment confirmed successfully for order #{}, transactionCode={}", order.getId(), transactionCode);
    }

    /**
     * Verify MONA Pay HMAC-SHA256 signature.
     */
    private void verifySignature(
            byte[] rawBody,
            String timestamp,
            String signature) {

        if (timestamp == null
                || signature == null
                || config.getWebhookSecret() == null
                || config.getWebhookSecret().isBlank()) {

            log.warn("Missing webhook authentication parameters. timestamp={}, signaturePresent={}, webhookSecretConfigured={}",
                    timestamp, signature != null, (config.getWebhookSecret() != null && !config.getWebhookSecret().isBlank()));

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing webhook authentication");
        }

        // Validate timestamp.
        long timestampValue;

        try {
            timestampValue = Long.parseLong(timestamp.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid webhook timestamp format: {}", timestamp);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid webhook timestamp");
        }

        long now = Instant.now().getEpochSecond();

        if (Math.abs(now - timestampValue) > 300) {
            log.warn("Webhook timestamp expired: serverTime={}, webhookTime={}, diffSeconds={}",
                    now, timestampValue, Math.abs(now - timestampValue));
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Webhook timestamp expired");
        }

        try {
            // Build signed message: timestamp + "." + rawBodyString
            String rawBodyString = new String(
                    rawBody != null ? rawBody : new byte[0],
                    StandardCharsets.UTF_8);

            String message = timestamp.trim() + "." + rawBodyString;

            // HMAC-SHA256
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    config.getWebhookSecret().trim().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");

            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    message.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            String calculatedHex = hex.toString();

            String incomingSig = signature.trim();
            if (incomingSig.toLowerCase().startsWith("sha256=")) {
                incomingSig = incomingSig.substring(7);
            }

            // Constant-time comparison
            boolean valid = MessageDigest.isEqual(
                    calculatedHex.getBytes(StandardCharsets.UTF_8),
                    incomingSig.toLowerCase().getBytes(StandardCharsets.UTF_8));

            if (!valid) {
                log.warn("MONA Pay signature mismatch. Calculated: sha256={}, Received: {}", calculatedHex, signature);
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid webhook signature");
            }

            log.info("MonaPay webhook signature verified successfully");

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unable to verify MONA Pay signature", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to verify MONA Pay signature");
        }
    }
}
