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

import java.net.URI;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;

    private final MonaPayClient monaPayClient;

    private final MonaPayConfig config;

    private final EmailService emailService;

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss 'ngày' dd/MM/yyyy");

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

        String baseOrigin = "https://deafeningly-conjugated-marisha.ngrok-free.dev";
        if (config.getReturnUrl() != null && config.getReturnUrl().startsWith("http")) {
            try {
                URI uri = URI.create(config.getReturnUrl());
                baseOrigin = uri.getScheme() + "://" + uri.getAuthority();
            } catch (Exception ignored) {
            }
        }
        String payUrl = baseOrigin + "/api/payments/" + order.getId() + "/pay-now";

        LocalDateTime expiresAt = order.getCreatedAt() != null
                ? order.getCreatedAt().plusSeconds(config.getExpiresIn())
                : LocalDateTime.now().plusSeconds(config.getExpiresIn());

        return new CreatePaymentResponse(
                order.getId(),
                order.getTotalAmount(),
                checkoutId,
                checkoutUrl,
                payUrl,
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
     * Auto process payment directly (called via QR scan from phone camera or 1-click button).
     */
    @Transactional
    public Order processAutoPay(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            // Best effort sandbox transaction creation on Mona Pay
            try {
                monaPayClient.createSandboxTransaction(
                        order.getTotalAmount().longValue(),
                        "ORDER-" + order.getId(),
                        order.getPaymentReference()
                );
            } catch (Exception e) {
                log.warn("Mona Pay sandbox transaction note: {}", e.getMessage());
            }

            order.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            if (order.getTransactionId() == null || order.getTransactionId().isBlank()) {
                order.setTransactionId("MONAPAY-AUTO-" + System.currentTimeMillis());
            }
            order.setPaidAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Order #{} marked as PAID via auto-pay", order.getId());

            // Gửi hóa đơn chi tiết về Gmail người nhận
            try {
                emailService.sendOrderInvoice(order);
            } catch (Exception e) {
                log.error("Failed to send order invoice email for order #{}: {}", order.getId(), e.getMessage());
            }
        }

        return order;
    }

    /**
     * Direct payment status without requiring user context.
     */
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatusDirect(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return new PaymentStatusResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getPaymentStatus().name(),
                order.getOrderStatus().name(),
                order.getTransactionId(),
                order.getPaidAt());
    }

    /**
     * Build responsive HTML confirmation page displayed when phone scans QR.
     */
    public String buildSuccessHtmlPage(Order order) {
        String paidDate = order.getPaidAt() != null
                ? order.getPaidAt().format(DATE_FORMAT)
                : LocalDateTime.now().format(DATE_FORMAT);

        String recipient = order.getShippingAddress() != null && order.getShippingAddress().getRecipientName() != null
                ? order.getShippingAddress().getRecipientName()
                : (order.getUser() != null && order.getUser().getFullName() != null ? order.getUser().getFullName() : "Quý khách");

        String recipientEmail = order.getShippingAddress() != null && order.getShippingAddress().getEmail() != null
                ? order.getShippingAddress().getEmail()
                : (order.getUser() != null ? order.getUser().getEmail() : "");

        String amountStr = CURRENCY_FORMAT.format(order.getTotalAmount() != null ? order.getTotalAmount().longValue() : 0);

        return "<!DOCTYPE html>\n"
                + "<html lang=\"vi\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <title>Thanh toán thành công - BookStore</title>\n"
                + "  <style>\n"
                + "    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background: #f0fdf4; margin: 0; padding: 24px; color: #1e293b; display: flex; justify-content: center; align-items: center; min-height: 90vh; }\n"
                + "    .card { background: #ffffff; border-radius: 16px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.1); max-width: 480px; width: 100%; overflow: hidden; border: 1px solid #bbf7d0; text-align: center; }\n"
                + "    .header { background: linear-gradient(135deg, #0d9488, #0f766e); padding: 32px 20px; color: white; }\n"
                + "    .icon { font-size: 54px; margin-bottom: 8px; animation: bounce 0.6s ease; }\n"
                + "    .title { font-size: 24px; font-weight: 700; margin: 0 0 6px 0; }\n"
                + "    .subtitle { font-size: 14px; opacity: 0.9; margin: 0; }\n"
                + "    .body { padding: 24px; text-align: left; }\n"
                + "    .info-box { background: #f8fafc; border-radius: 12px; padding: 16px; border: 1px solid #e2e8f0; margin-bottom: 20px; }\n"
                + "    .row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px; }\n"
                + "    .row:last-child { margin-bottom: 0; }\n"
                + "    .label { color: #64748b; }\n"
                + "    .val { font-weight: 600; color: #0f172a; text-align: right; }\n"
                + "    .highlight { color: #0f766e; font-size: 18px; font-weight: 700; }\n"
                + "    .email-box { background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 10px; padding: 14px; text-align: center; margin-bottom: 20px; }\n"
                + "    .email-title { font-weight: 700; color: #065f46; font-size: 15px; margin-bottom: 4px; }\n"
                + "    .email-target { color: #047857; font-weight: 600; font-size: 14px; word-break: break-all; }\n"
                + "    .note { font-size: 13px; color: #64748b; text-align: center; line-height: 1.5; }\n"
                + "    @keyframes bounce { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.15); } }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div class=\"card\">\n"
                + "    <div class=\"header\">\n"
                + "      <div class=\"icon\">🎉</div>\n"
                + "      <h1 class=\"title\">Thanh Toán Thành Công!</h1>\n"
                + "      <p class=\"subtitle\">Cổng thanh toán Mona Pay đã xác nhận</p>\n"
                + "    </div>\n"
                + "    <div class=\"body\">\n"
                + "      <div class=\"email-box\">\n"
                + "        <div class=\"email-title\">📧 Hóa đơn chi tiết đã được gửi về Gmail</div>\n"
                + "        <div class=\"email-target\">" + recipientEmail + "</div>\n"
                + "      </div>\n"
                + "      <div class=\"info-box\">\n"
                + "        <div class=\"row\">\n"
                + "          <span class=\"label\">Mã đơn hàng:</span>\n"
                + "          <span class=\"val\">#ORDER-" + order.getId() + "</span>\n"
                + "        </div>\n"
                + "        <div class=\"row\">\n"
                + "          <span class=\"label\">Người nhận:</span>\n"
                + "          <span class=\"val\">" + recipient + "</span>\n"
                + "        </div>\n"
                + "        <div class=\"row\">\n"
                + "          <span class=\"label\">Thời gian:</span>\n"
                + "          <span class=\"val\">" + paidDate + "</span>\n"
                + "        </div>\n"
                + "        <div class=\"row\">\n"
                + "          <span class=\"label\">Mã giao dịch:</span>\n"
                + "          <span class=\"val\"><code>" + (order.getTransactionId() != null ? order.getTransactionId() : "MONAPAY-AUTO") + "</code></span>\n"
                + "        </div>\n"
                + "        <div class=\"row\">\n"
                + "          <span class=\"label\">Số tiền đã trả:</span>\n"
                + "          <span class=\"val highlight\">" + amountStr + " đ</span>\n"
                + "        </div>\n"
                + "      </div>\n"
                + "      <p class=\"note\">✓ Màn hình đặt hàng trên máy tính của bạn sẽ tự động xác nhận ngay lập tức.<br>Cảm ơn bạn đã mua sắm tại <strong>BookStore Vietnam</strong>!</p>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>";
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
