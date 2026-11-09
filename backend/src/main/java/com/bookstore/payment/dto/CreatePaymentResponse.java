package com.bookstore.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePaymentResponse(

        Long orderId,

        BigDecimal amount,

        String checkoutId,

        String checkoutUrl,

        String paymentStatus,

        LocalDateTime expiresAt

) {
}