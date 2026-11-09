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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

            throw new BadRequestException(
                    "This order has already been paid");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {

            throw new BadRequestException(
                    "This order cannot be paid");
        }

        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {

            throw new BadRequestException(
                    "This order does not use online payment");
        }

        /*
         * If a checkout already exists, reuse it.
         */
        if (order.getPaymentReference() != null
                && !order.getPaymentReference().isBlank()) {

            JsonNode existingResponse = monaPayClient.getCheckout(
                    order.getPaymentReference());

            JsonNode existing = monaPayClient.extractData(
                    existingResponse);

            String status = existing.path("status").asText();

            if ("pending".equalsIgnoreCase(status)) {

                return new CreatePaymentResponse(
                        order.getId(),
                        order.getTotalAmount(),
                        order.getPaymentReference(),
                        existing.path("checkout_url").asText(),
                        order.getPaymentStatus().name(),
                        order.getCreatedAt().plusSeconds(
                                config.getExpiresIn()));
            }

            throw new BadRequestException(
                    "Existing checkout is no longer pending. "
                            + "Please create a new order or handle checkout expiry.");
        }

        /*
         * Build unique order code.
         */
        String orderCode = "ORDER-" + order.getId();

        /*
         * Create Checkout.
         */
        JsonNode response = monaPayClient.createCheckout(
                order.getTotalAmount().longValueExact(),
                orderCode,
                "Payment for bookstore order " + orderCode);

        JsonNode data = monaPayClient.extractData(response);

        String checkoutId = data.path("id").asText(null);

        String checkoutUrl = data.path("checkout_url").asText(null);

        if (checkoutId == null || checkoutUrl == null
                || checkoutUrl.isBlank()) {

            throw new IllegalStateException(
                    "MONA Pay did not return checkout information");
        }

        /*
         * Save MONA Pay Checkout ID.
         */
        order.setPaymentReference(checkoutId);

        orderRepository.save(order);

        return new CreatePaymentResponse(
                order.getId(),
                order.getTotalAmount(),
                checkoutId,
                checkoutUrl,
                PaymentStatus.UNPAID.name(),
                LocalDateTime.now().plusSeconds(config.getExpiresIn()));
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