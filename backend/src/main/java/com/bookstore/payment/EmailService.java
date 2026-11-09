package com.bookstore.payment;

import com.bookstore.order.Order;
import com.bookstore.order.OrderItem;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss 'ngày' dd/MM/yyyy");

    /**
     * Send detailed order invoice to customer's Gmail.
     */
    public void sendOrderInvoice(Order order) {
        if (order == null) {
            log.warn("Cannot send invoice: order is null");
            return;
        }

        String toEmail = null;
        if (order.getShippingAddress() != null && order.getShippingAddress().getEmail() != null && !order.getShippingAddress().getEmail().isBlank()) {
            toEmail = order.getShippingAddress().getEmail().trim();
        } else if (order.getUser() != null && order.getUser().getEmail() != null && !order.getUser().getEmail().isBlank()) {
            toEmail = order.getUser().getEmail().trim();
        }

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send invoice: recipient email is missing for order #{}", order.getId());
            return;
        }

        String customerName = order.getShippingAddress() != null && order.getShippingAddress().getRecipientName() != null
                ? order.getShippingAddress().getRecipientName()
                : (order.getUser() != null && order.getUser().getFullName() != null ? order.getUser().getFullName() : "Quý khách");

        String subject = "[BookStore] Hóa đơn thanh toán thành công đơn hàng #ORDER-" + order.getId();
        String htmlContent = buildInvoiceHtml(order, customerName, toEmail);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null || mailUsername == null || mailUsername.isBlank()) {
            log.info("Spring Mail credentials not configured. Simulating email invoice sent to: {}\nSubject: {}\n(To send real emails, please configure spring.mail.username and spring.mail.password in application-local.yml)",
                    toEmail, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailUsername, "BookStore Vietnam");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Successfully delivered order invoice email to {} for order #{}", toEmail, order.getId());

        } catch (Exception e) {
            log.error("Failed to send order invoice email to {} for order #{}: {}", toEmail, order.getId(), e.getMessage());
            log.info("Invoice HTML content:\n{}", htmlContent);
        }
    }

    private String buildInvoiceHtml(Order order, String customerName, String recipientEmail) {
        String paidDate = order.getPaidAt() != null
                ? order.getPaidAt().format(DATE_FORMAT)
                : (order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FORMAT) : "Vừa xong");

        String shippingAddressStr = order.getShippingAddress() != null
                ? (order.getShippingAddress().getLine1() + ", "
                + order.getShippingAddress().getDistrict() + ", "
                + order.getShippingAddress().getCity())
                : "Địa chỉ theo hồ sơ tài khoản";

        String phoneStr = order.getShippingAddress() != null && order.getShippingAddress().getPhone() != null
                ? order.getShippingAddress().getPhone()
                : (order.getUser() != null && order.getUser().getPhone() != null ? order.getUser().getPhone() : "Chưa cập nhật");

        StringBuilder itemsRows = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                long price = item.getPriceAtPurchase() != null ? item.getPriceAtPurchase().longValue() : 0;
                long subtotal = price * item.getQuantity();
                String name = item.getProduct() != null ? item.getProduct().getName() : "Sách Bookstore";

                itemsRows.append("<tr>")
                        .append("<td style=\"padding:12px 14px; border-bottom:1px solid #e2e8f0; font-size:14px; color:#1e293b;\">")
                        .append(name)
                        .append("</td>")
                        .append("<td style=\"padding:12px 14px; border-bottom:1px solid #e2e8f0; font-size:14px; text-align:center; color:#475569;\">")
                        .append(item.getQuantity())
                        .append("</td>")
                        .append("<td style=\"padding:12px 14px; border-bottom:1px solid #e2e8f0; font-size:14px; text-align:right; color:#475569;\">")
                        .append(CURRENCY_FORMAT.format(price)).append(" đ")
                        .append("</td>")
                        .append("<td style=\"padding:12px 14px; border-bottom:1px solid #e2e8f0; font-size:14px; text-align:right; font-weight:600; color:#0f172a;\">")
                        .append(CURRENCY_FORMAT.format(subtotal)).append(" đ")
                        .append("</td>")
                        .append("</tr>");
            }
        }

        String totalStr = CURRENCY_FORMAT.format(order.getTotalAmount() != null ? order.getTotalAmount().longValue() : 0);

        return "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px;\">"
                + "  <div style=\"max-width: 620px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);\">"
                + "    <div style=\"background: linear-gradient(135deg, #0d9488 0%, #0f766e 100%); padding: 32px 24px; text-align: center; color: #ffffff;\">"
                + "      <h1 style=\"margin: 0 0 8px 0; font-size: 26px; font-weight: 700; letter-spacing: 0.5px;\">📚 BOOKSTORE VIETNAM</h1>"
                + "      <p style=\"margin: 0; font-size: 15px; opacity: 0.95;\">HÓA ĐƠN XÁC NHẬN THANH TOÁN TRỰC TUYẾN</p>"
                + "    </div>"
                + "    <div style=\"padding: 24px 28px;\">"
                + "      <p style=\"font-size: 15px; color: #334155; margin-top: 0;\">Xin chào <strong>" + customerName + "</strong>,</p>"
                + "      <p style=\"font-size: 14px; color: #475569; line-height: 1.6;\">Cảm ơn bạn đã tin tưởng mua sắm tại <strong>BookStore</strong>. Hệ thống xác nhận đơn hàng của bạn đã được thanh toán trực tuyến thành công qua cổng thanh toán <strong>Mona Pay</strong>.</p>"
                + "      <div style=\"background: #f0fdfa; border: 1px solid #ccfbf1; border-radius: 8px; padding: 16px; margin: 20px 0;\">"
                + "        <div style=\"display:flex; justify-content:space-between; margin-bottom:8px; font-size:14px;\">"
                + "          <span style=\"color:#0f766e; font-weight:600;\">Mã đơn hàng:</span>"
                + "          <strong style=\"color:#0f172a;\">#ORDER-" + order.getId() + "</strong>"
                + "        </div>"
                + "        <div style=\"display:flex; justify-content:space-between; margin-bottom:8px; font-size:14px;\">"
                + "          <span style=\"color:#0f766e; font-weight:600;\">Thời gian thanh toán:</span>"
                + "          <span style=\"color:#334155;\">" + paidDate + "</span>"
                + "        </div>"
                + "        <div style=\"display:flex; justify-content:space-between; margin-bottom:8px; font-size:14px;\">"
                + "          <span style=\"color:#0f766e; font-weight:600;\">Phương thức thanh toán:</span>"
                + "          <span style=\"color:#334155;\">Mona Pay (Quét mã QR)</span>"
                + "        </div>"
                + "        <div style=\"display:flex; justify-content:space-between; margin-bottom:8px; font-size:14px;\">"
                + "          <span style=\"color:#0f766e; font-weight:600;\">Mã giao dịch:</span>"
                + "          <code style=\"background:#e2e8f0; padding:2px 6px; border-radius:4px; font-size:13px;\">" + (order.getTransactionId() != null ? order.getTransactionId() : "MONAPAY-SUCCESS") + "</code>"
                + "        </div>"
                + "        <div style=\"display:flex; justify-content:space-between; font-size:14px;\">"
                + "          <span style=\"color:#0f766e; font-weight:600;\">Trạng thái:</span>"
                + "          <span style=\"color:#16a34a; font-weight:700;\">✓ ĐÃ THANH TOÁN THÀNH CÔNG</span>"
                + "        </div>"
                + "      </div>"
                + "      <h3 style=\"font-size: 16px; color: #0f172a; margin: 24px 0 12px 0; border-bottom: 2px solid #f1f5f9; padding-bottom: 8px;\">Chi tiết kiện sách đã đặt</h3>"
                + "      <table style=\"width: 100%; border-collapse: collapse; margin-bottom: 20px;\">"
                + "        <thead>"
                + "          <tr style=\"background: #f8fafc;\">"
                + "            <th style=\"padding: 10px 14px; text-align: left; font-size: 13px; color: #64748b; border-bottom: 2px solid #e2e8f0;\">Tên sách</th>"
                + "            <th style=\"padding: 10px 14px; text-align: center; font-size: 13px; color: #64748b; border-bottom: 2px solid #e2e8f0;\">SL</th>"
                + "            <th style=\"padding: 10px 14px; text-align: right; font-size: 13px; color: #64748b; border-bottom: 2px solid #e2e8f0;\">Đơn giá</th>"
                + "            <th style=\"padding: 10px 14px; text-align: right; font-size: 13px; color: #64748b; border-bottom: 2px solid #e2e8f0;\">Thành tiền</th>"
                + "          </tr>"
                + "        </thead>"
                + "        <tbody>"
                + itemsRows.toString()
                + "        </tbody>"
                + "      </table>"
                + "      <div style=\"text-align: right; margin-bottom: 24px;\">"
                + "        <p style=\"margin: 4px 0; font-size: 14px; color: #64748b;\">Phí vận chuyển: <strong style=\"color:#16a34a;\">Miễn phí</strong></p>"
                + "        <p style=\"margin: 8px 0; font-size: 18px; color: #0f172a;\">Tổng số tiền thanh toán: <strong style=\"color:#0d9488; font-size: 20px;\">" + totalStr + " đ</strong></p>"
                + "      </div>"
                + "      <div style=\"background: #f8fafc; border-radius: 8px; padding: 14px 18px; font-size: 13px; color: #475569; line-height: 1.5;\">"
                + "        <strong style=\"color:#0f172a;\">📍 Địa chỉ nhận hàng:</strong> " + customerName + " (" + phoneStr + (recipientEmail != null ? " • " + recipientEmail : "") + ") - " + shippingAddressStr + "<br>"
                + "        <em>* Đơn hàng sẽ sớm được đóng gói và giao đến quý khách trong thời gian sớm nhất.</em>"
                + "      </div>"
                + "    </div>"
                + "    <div style=\"background: #f1f5f9; padding: 18px 24px; text-align: center; font-size: 12px; color: #64748b;\">"
                + "      <p style=\"margin: 0 0 4px 0;\">Hệ thống Nhà sách Trực tuyến BookStore Vietnam</p>"
                + "      <p style=\"margin: 0;\">Nếu bạn có thắc mắc, vui lòng liên hệ hotline: 1900 636 648 hoặc gửi phản hồi qua website.</p>"
                + "    </div>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }
}
