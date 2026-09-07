package com.bookstore.chatbot;

import org.springframework.stereotype.Component;

/**
 * Very small, dependency-free keyword classifier that decides *which* data the ChatbotService needs
 * to fetch and simplify before calling Groq (UC14.1.2 vs UC14.1.3 vs UC14.1.4). Kept rule-based on
 * purpose: it runs before any API call, so it must be free and instant. Swap for a proper NLU/ML
 * classifier later without touching the rest of the pipeline.
 */
@Component
public class IntentDetector {

    public ChatIntent detect(String message) {
        String m = message.toLowerCase();
        if (containsAny(m, "don hang", "đơn hàng", "order", "ma don", "trang thai giao hang", "van chuyen")) {
            return ChatIntent.ORDER_LOOKUP;
        }
        if (containsAny(m, "goi y", "gợi ý", "recommend", "nen mua gi", "nên mua gì", "phu hop")) {
            return ChatIntent.RECOMMENDATION;
        }
        if (containsAny(m, "gap nhan vien", "gặp nhân viên", "noi chuyen voi nguoi", "khong hai long",
                "human", "hotro truc tiep")) {
            return ChatIntent.HANDOFF;
        }
        if (containsAny(m, "gia", "giá", "ton kho", "tồn kho", "con hang", "còn hàng", "sach", "sách", "mo ta")) {
            return ChatIntent.PRODUCT_INFO;
        }
        return ChatIntent.GENERAL;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) if (text.contains(k)) return true;
        return false;
    }
}
