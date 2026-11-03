package com.bookstore.chatbot;

import com.bookstore.order.Order;
import com.bookstore.order.OrderItem;
import com.bookstore.product.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the "hàm viết sẵn để đơn giản hóa dữ liệu đầu vào" the requirements
 * call for: every piece
 * of DB data that needs to reach Groq passes through here first. It exists for
 * three reasons:
 * 1) Token cost - full JPA entities carry audit fields, lazy proxies, nested
 * relations etc. that
 * the LLM never needs and that cost tokens/latency.
 * 2) Privacy - never leak password hashes, emails, phone numbers, internal IDs,
 * or other users'
 * data into a third-party API call.
 * 3) Prompt stability - a fixed, flat text format is far easier to prompt
 * against reliably than
 * a raw JSON dump of an entity graph.
 * Every method returns a single plain-text line (or a small list of lines)
 * ready to be concatenated
 * straight into the Groq system prompt by GroqPromptBuilder.
 */
@Component
public class DataSimplifier {

    // Used for UC14.1.2 (hỏi đáp thông tin sản phẩm) and UC14.1.4 (gợi ý sản phẩm)
    public String simplifyProduct(Product p) {
        String stock = p.getStockQuantity() > 0 ? ("con " + p.getStockQuantity()) : "het hang";
        String category = p.getCategory() != null ? p.getCategory().getName() : "khac";
        return String.format("- %s | gia: %,.0f VND | ton kho: %s | danh muc: %s",
                truncate(p.getName(), 80), p.getPrice(), stock, category);
    }

    public String simplifyProducts(List<Product> products, int maxItems) {
        return products.stream().limit(maxItems).map(this::simplifyProduct)
                .collect(Collectors.joining("\n"));
    }

    // Used for UC14.1.3 (tra cứu đơn hàng qua chatbot) - deliberately omits
    // address/phone/email
    public String simplifyOrder(Order order) {
        String items = order.getItems().stream()
                .map(this::simplifyOrderItem)
                .collect(Collectors.joining(", "));
        return String.format("Don hang #%d | trang thai: %s | tong tien: %,.0f VND | san pham: %s",
                order.getId(), order.getOrderStatus(), order.getTotalAmount(), items);
    }

    private String simplifyOrderItem(OrderItem i) {
        return truncate(i.getProduct().getName(), 40) + " x" + i.getQuantity();
    }

    // Used to feed the admin-curated knowledge base (UC14.2.2 / UC14.2.5) into the
    // system prompt
    public String simplifyFaq(List<FaqEntry> faqs, int maxItems) {
        return faqs.stream().limit(maxItems)
                .map(f -> "Q: " + truncate(f.getQuestion(), 150) + " | A: " + truncate(f.getAnswer(), 300))
                .collect(Collectors.joining("\n"));
    }

    private String truncate(String s, int max) {
        if (s == null)
            return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
