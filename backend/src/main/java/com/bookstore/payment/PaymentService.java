package com.bookstore.payment;

import com.bookstore.common.BadRequestException;
import com.bookstore.common.ResourceNotFoundException;

import com.bookstore.order.Order;
import com.bookstore.order.OrderRepository;
import com.bookstore.order.OrderStatus;
import com.bookstore.order.PaymentMethod;
import com.bookstore.order.PaymentStatus;

import com.bookstore.payment.dto.CreatePaymentResponse;
import com.bookstore.payment.dto.PaymentStatusResponse;

import com.bookstore.payment.monapay.MonaPayClient;
import com.bookstore.payment.monapay.MonaPayConfig;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;

    private final MonaPayClient monaPayClient;

    private final MonaPayConfig config;

    /**
     * Create payment for an existing order.
     */
    @Transactional
    public CreatePaymentResponse createPayment(
            Long userId,
            Long orderId) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Already paid.
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("This order has already been paid");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("This order cannot be paid");
        }

        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new BadRequestException("This order does not use online payment");
        }

        String orderCode = "ORDER-" + order.getId();

        /*
         * If a checkout already exists, reuse it.
         */
        if (order.getPaymentReference() != null
                && !order.getPaymentReference().isBlank()) {

            try {
                JsonNode existingResponse = monaPayClient.getCheckout(order.getPaymentReference());
                JsonNode existing = monaPayClient.extractData(existingResponse);

                String status = existing.path("status").asText();

                if ("pending".equalsIgnoreCase(status)) {
                    return buildCreatePaymentResponse(order, existing);
                }
            } catch (Exception e) {
                log.warn("Could not reuse existing checkout {}: {}. Generating new checkout.",
                        order.getPaymentReference(), e.getMessage());
            }
        }

        /*
         * Create Checkout on MONA Pay.
         */
        JsonNode response = monaPayClient.createCheckout(
                order.getTotalAmount().longValue(),
                orderCode,
                "Payment for bookstore order " + orderCode);

        JsonNode data = monaPayClient.extractData(response);

        String checkoutId = data.path("id").asText(null);
        String checkoutUrl = data.path("checkout_url").asText(null);

        if (checkoutId == null || checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalStateException("MONA Pay did not return checkout information");
        }

        /*
         * Save MONA Pay Checkout ID.
         */
        order.setPaymentReference(checkoutId);
        orderRepository.save(order);

        return buildCreatePaymentResponse(order, data);
    }

    private CreatePaymentResponse buildCreatePaymentResponse(Order order, JsonNode data) {
        String checkoutId = data.path("id").asText(order.getPaymentReference());
        String checkoutUrl = data.path("checkout_url").asText(null);
        String qrImageUrl = data.path("qr_image_url").asText(null);
        String qrDataUrl = data.path("qr_data_url").asText(null);

        JsonNode bankNode = data.path("bank");
        String bankCode = bankNode.path("code").asText(null);
        String bankName = bankNode.path("name").asText(null);
        String bankAccount = bankNode.path("account_number").asText(null);
        String accountName = bankNode.path("account_name").asText(null);
        String transferContent = bankNode.path("transfer_content").asText("ORDER-" + order.getId());

        LocalDateTime expiresAt = order.getCreatedAt() != null
                ? order.getCreatedAt().plusSeconds(config.getExpiresIn())
                : LocalDateTime.now().plusSeconds(config.getExpiresIn());

        return new CreatePaymentResponse(
                order.getId(),
                order.getTotalAmount(),
                checkoutId,
                checkoutUrl,
                order.getPaymentStatus().name(),
                expiresAt,
                qrImageUrl,
                qrDataUrl,
                bankCode,
                bankName,
                bankAccount,
                accountName,
                transferContent
        );
    }

    /**
     * Get payment status.
     */
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(
            Long userId,
            Long orderId) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return new PaymentStatusResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getPaymentStatus().name(),
                order.getOrderStatus().name(),
                order.getTransactionId(),
                order.getPaidAt());
    }
}
