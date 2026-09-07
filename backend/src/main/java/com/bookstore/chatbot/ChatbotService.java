package com.bookstore.chatbot;

import com.bookstore.auth.User;
import com.bookstore.auth.UserRepository;
import com.bookstore.chatbot.dto.*;
import com.bookstore.common.BadRequestException;
import com.bookstore.common.ResourceNotFoundException;
import com.bookstore.order.Order;
import com.bookstore.order.OrderRepository;
import com.bookstore.product.Product;
import com.bookstore.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates Epic 14.1. This is the module that links Theme 14 to everything else in the system:
 * it reads Product (Theme 2/8) and Order (Theme 4/5) data, always through DataSimplifier, then calls
 * GroqClient and persists the exchange as ChatMessage rows.
 *
 * Flow for every incoming message (UC14.1.2 / UC14.1.3 / UC14.1.4 all share this same pipeline):
 *   1) IntentDetector decides what kind of data is relevant.
 *   2) The relevant entities are fetched from their own repositories (never duplicated here).
 *   3) DataSimplifier turns them into compact, safe text.
 *   4) GroqPromptBuilder assembles the final message list (system + short history + user turn).
 *   5) GroqClient calls the API; the reply is saved as a BOT ChatMessage and returned.
 */
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private static final int MAX_HISTORY_TURNS = 6;

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final FaqRepository faqRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private final IntentDetector intentDetector;
    private final DataSimplifier dataSimplifier;
    private final GroqPromptBuilder promptBuilder;
    private final GroqClient groqClient;

    // UC14.1.1 - Bắt đầu trò chuyện với chatbot (userId may be null for a guest visitor)
    public Long startSession(Long userId) {
        ChatSession session = new ChatSession();
        if (userId != null) {
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            session.setUser(user);
        }
        session.setStatus(ChatSessionStatus.BOT);
        return sessionRepository.save(session).getId();
    }

    // UC14.1.2 (product Q&A) / UC14.1.3 (order lookup) / UC14.1.4 (recommendation) all enter here
    public ChatMessageDto sendMessage(Long userId, Long sessionId, SendMessageRequest req) {
        ChatSession session = requireSession(sessionId);
        if (session.getStatus() == ChatSessionStatus.HANDED_OFF) {
            throw new BadRequestException("This conversation was handed off to a support agent");
        }

        ChatMessage userMsg = persist(session, SenderType.USER, req.getContent());

        ChatIntent intent = intentDetector.detect(req.getContent());
        String context = buildContext(intent, userId);

        List<ChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<GroqMessage> prompt = promptBuilder.build(context, history, MAX_HISTORY_TURNS);

        String reply = groqClient.chat(prompt);
        ChatMessage botMsg = persist(session, SenderType.BOT, reply);

        return toDto(botMsg);
    }

    // UC14.1.5 - Chuyển yêu cầu sang nhân viên hỗ trợ
    public void requestHandoff(Long sessionId) {
        ChatSession session = requireSession(sessionId);
        session.setStatus(ChatSessionStatus.HANDED_OFF);
        sessionRepository.save(session);
        persist(session, SenderType.BOT, "Da chuyen cuoc tro chuyen nay cho nhan vien ho tro, vui long cho trong giay lat.");
    }

    // UC14.1.6 - Đánh giá câu trả lời của chatbot
    public void rateMessage(Long messageId, RateMessageRequest req) {
        ChatMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        if (msg.getSender() != SenderType.BOT) throw new BadRequestException("Only bot replies can be rated");
        msg.setRating(req.getRating());
        messageRepository.save(msg);
    }

    public List<ChatMessageDto> history(Long sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream().map(this::toDto).toList();
    }

    /** Gathers and simplifies exactly the data the current intent needs - nothing more is ever sent to Groq. */
    private String buildContext(ChatIntent intent, Long userId) {
        List<String> parts = new ArrayList<>();
        List<FaqEntry> faqs = faqRepository.findByActiveTrue();
        if (!faqs.isEmpty()) {
            parts.add("Cau hoi thuong gap:\n" + dataSimplifier.simplifyFaq(faqs, 10));
        }
        switch (intent) {
            case PRODUCT_INFO, RECOMMENDATION -> {
                List<Product> products = productRepository.findAll();
                parts.add("Danh sach san pham hien co:\n" + dataSimplifier.simplifyProducts(products, 30));
            }
            case ORDER_LOOKUP -> {
                if (userId == null) {
                    parts.add("Nguoi dung chua dang nhap nen khong the tra cuu don hang, hay de nghi ho dang nhap.");
                } else {
                    List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
                    if (orders.isEmpty()) {
                        parts.add("Nguoi dung nay chua co don hang nao.");
                    } else {
                        parts.add("Don hang gan day cua nguoi dung:\n" +
                                orders.stream().limit(5).map(dataSimplifier::simplifyOrder)
                                        .reduce((a, b) -> a + "\n" + b).orElse(""));
                    }
                }
            }
            case HANDOFF -> parts.add("Nguoi dung muon noi chuyen voi nhan vien ho tro; hay xac nhan va huong dan.");
            default -> { /* GENERAL: FAQ context alone is enough */ }
        }
        return String.join("\n\n", parts);
    }

    private ChatMessage persist(ChatSession session, SenderType sender, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSession(session);
        msg.setSender(sender);
        msg.setContent(content);
        return messageRepository.save(msg);
    }

    private ChatSession requireSession(Long id) {
        return sessionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
    }

    private ChatMessageDto toDto(ChatMessage m) {
        return new ChatMessageDto(m.getId(), m.getSender().name(), m.getContent(), m.getRating(), m.getCreatedAt());
    }
}
