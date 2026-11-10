package com.bookstore.payment;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.CurrentUser;
import com.bookstore.order.Order;

import com.bookstore.payment.dto.CreatePaymentResponse;
import com.bookstore.payment.dto.PaymentStatusResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    /**
     * Auto payment endpoint via browser (phone camera QR scan).
     * Automatically completes payment, sends invoice email, and displays confirmation page.
     */
    @GetMapping(value = "/{orderId}/pay-now", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> payNowHtml(@PathVariable Long orderId) {
        Order order = paymentService.processAutoPay(orderId);
        return ResponseEntity.ok(paymentService.buildSuccessHtmlPage(order));
    }

    /**
     * Auto payment endpoint via AJAX/API.
     */
    @PostMapping("/{orderId}/pay-now")
    public ApiResponse<PaymentStatusResponse> payNowJson(@PathVariable Long orderId) {
        paymentService.processAutoPay(orderId);
        return ApiResponse.ok("Payment confirmed", paymentService.getPaymentStatusDirect(orderId));
    }
}