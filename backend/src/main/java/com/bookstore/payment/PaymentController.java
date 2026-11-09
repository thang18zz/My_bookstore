package com.bookstore.payment;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.CurrentUser;

import com.bookstore.payment.dto.CreatePaymentResponse;
import com.bookstore.payment.dto.PaymentStatusResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create payment checkout.
     */
    @PostMapping("/{orderId}/create")
    public ApiResponse<CreatePaymentResponse> createPayment(
            @PathVariable Long orderId) {

        return ApiResponse.ok(
                "Payment checkout created",
                paymentService.createPayment(
                        CurrentUser.id(),
                        orderId));
    }

    /**
     * Get payment status.
     */
    @GetMapping("/{orderId}/status")
    public ApiResponse<PaymentStatusResponse> getPaymentStatus(
            @PathVariable Long orderId) {

        return ApiResponse.ok(
                paymentService.getPaymentStatus(
                        CurrentUser.id(),
                        orderId));
    }
}