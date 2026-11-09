package com.bookstore.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentStatusResponse(

        Long orderId,

        BigDecimal amount,

        String paymentStatus,

        String orderStatus,

        String transactionId,

        LocalDateTime paidAt

) {
}