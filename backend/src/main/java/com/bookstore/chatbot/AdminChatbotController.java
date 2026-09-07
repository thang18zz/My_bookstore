package com.bookstore.chatbot;

import com.bookstore.chatbot.dto.*;
import com.bookstore.common.ApiResponse;
import com.bookstore.common.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Epic 14.2 - Quản trị chatbot (Admin): UC14.2.1-5. */
@RestController
@RequestMapping("/api/admin/chatbot")
@RequiredArgsConstructor
public class AdminChatbotController {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final FaqRepository faqRepository;
    private final ChatbotService chatbotService;

    @GetMapping("/sessions") // UC14.2.1 - Xem lịch sử hội thoại chatbot
    public ApiResponse<Page<ChatSession>> sessions(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(sessionRepository.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/sessions/{id}/messages") // UC14.2.1 detail
    public ApiResponse<List<ChatMessageDto>> sessionMessages(@PathVariable Long id) {
        return ApiResponse.ok(chatbotService.history(id));
    }

    @GetMapping("/handoff-queue") // UC14.2.3 - Tiếp nhận hội thoại được chuyển từ chatbot
    public ApiResponse<Page<ChatSession>> handoffQueue(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(sessionRepository.findByStatus(ChatSessionStatus.HANDED_OFF, PageRequest.of(page, size)));
    }

    @PostMapping("/sessions/{id}/reply") // UC14.2.3 - admin replies inside a handed-off session
    public ApiResponse<Void> adminReply(@PathVariable Long id, @RequestParam String content) {
        ChatSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        ChatMessage msg = new ChatMessage();
        msg.setSession(session);
        msg.setSender(SenderType.ADMIN);
        msg.setContent(content);
        messageRepository.save(msg);
        return ApiResponse.ok("Reply sent", null);
    }

    @PostMapping("/sessions/{id}/close")
    public ApiResponse<Void> close(@PathVariable Long id) {
        ChatSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        session.setStatus(ChatSessionStatus.CLOSED);
        sessionRepository.save(session);
        return ApiResponse.ok("Session closed", null);
    }

    @GetMapping("/stats") // UC14.2.4 - Xem thống kê hiệu quả chatbot
    public ApiResponse<ChatbotStatsDto> stats() {
        long total = sessionRepository.count();
        long handedOff = sessionRepository.countByStatus(ChatSessionStatus.HANDED_OFF);
        double handoffRate = total == 0 ? 0 : (handedOff * 100.0 / total);
        List<ChatMessage> rated = messageRepository.findBySenderAndRatingIsNotNull(SenderType.BOT);
        double avgRating = rated.isEmpty() ? 0 : rated.stream().mapToInt(ChatMessage::getRating).average().orElse(0);
        return ApiResponse.ok(new ChatbotStatsDto(total, handedOff, handoffRate, avgRating));
    }

    // ---- FAQ / knowledge base: UC14.2.2 (kịch bản/FAQ) + UC14.2.5 (cập nhật tri thức) ----

    @GetMapping("/faqs")
    public ApiResponse<List<FaqEntry>> listFaqs() {
        return ApiResponse.ok(faqRepository.findAll());
    }

    @PostMapping("/faqs")
    public ApiResponse<FaqEntry> createFaq(@Valid @RequestBody FaqRequest req) {
        FaqEntry f = new FaqEntry();
        applyFaq(f, req);
        return ApiResponse.ok("FAQ created", faqRepository.save(f));
    }

    @PutMapping("/faqs/{id}")
    public ApiResponse<FaqEntry> updateFaq(@PathVariable Long id, @Valid @RequestBody FaqRequest req) {
        FaqEntry f = faqRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("FAQ not found"));
        applyFaq(f, req);
        return ApiResponse.ok("FAQ updated", faqRepository.save(f));
    }

    @DeleteMapping("/faqs/{id}")
    public ApiResponse<Void> deleteFaq(@PathVariable Long id) {
        faqRepository.deleteById(id);
        return ApiResponse.ok("FAQ deleted", null);
    }

    private void applyFaq(FaqEntry f, FaqRequest req) {
        f.setQuestion(req.getQuestion());
        f.setAnswer(req.getAnswer());
        f.setKeywords(req.getKeywords());
        f.setActive(true);
    }
}
