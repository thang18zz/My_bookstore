package com.bookstore.chatbot;

import com.bookstore.chatbot.dto.*;
import com.bookstore.common.ApiResponse;
import com.bookstore.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Epic 14.1 - Chatbot hỗ trợ khách hàng. Requires a logged-in customer (the spec's user stories are
 *  all "Là Khách hàng..."), so this is intentionally NOT public - see SecurityConfig. */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/sessions") // UC14.1.1
    public ApiResponse<StartSessionResponse> start() {
        return ApiResponse.ok("Session started", new StartSessionResponse(chatbotService.startSession(CurrentUser.id())));
    }

    @PostMapping("/sessions/{sessionId}/messages") // UC14.1.2 / UC14.1.3 / UC14.1.4
    public ApiResponse<ChatMessageDto> send(@PathVariable Long sessionId, @Valid @RequestBody SendMessageRequest req) {
        return ApiResponse.ok(chatbotService.sendMessage(CurrentUser.id(), sessionId, req));
    }

    @GetMapping("/sessions/{sessionId}/messages") // load history when reopening the widget
    public ApiResponse<List<ChatMessageDto>> history(@PathVariable Long sessionId) {
        return ApiResponse.ok(chatbotService.history(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/handoff") // UC14.1.5
    public ApiResponse<Void> handoff(@PathVariable Long sessionId) {
        chatbotService.requestHandoff(sessionId);
        return ApiResponse.ok("Transferred to support staff", null);
    }

    @PostMapping("/messages/{messageId}/rate") // UC14.1.6
    public ApiResponse<Void> rate(@PathVariable Long messageId, @Valid @RequestBody RateMessageRequest req) {
        chatbotService.rateMessage(messageId, req);
        return ApiResponse.ok("Thanks for your feedback", null);
    }
}
