package com.bookstore.chatbot;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the final list of GroqMessage sent to the API: one system message carrying the
 * simplified context (already produced by DataSimplifier), a short slice of prior turns for
 * continuity, and the latest user turn. Kept separate from ChatbotService so the prompt shape
 * can be tuned/tested independently of the persistence/orchestration logic.
 */
@Component
public class GroqPromptBuilder {

    private static final String SYSTEM_PREAMBLE = """
            Ban la tro ly ao (chatbot) cua mot cua hang sach truc tuyen (Book Store).
            Chi tra loi dua tren thong tin duoc cung cap ben duoi, khong bia dat gia hoac ton kho.
            Neu khong co du thong tin de tra loi, hay de nghi khach hang lien he nhan vien ho tro.
            Tra loi ngan gon, than thien, bang tieng Viet.
            """;

    public List<GroqMessage> build(String simplifiedContext, List<ChatMessage> history, int maxHistoryTurns) {
        List<GroqMessage> messages = new ArrayList<>();
        messages.add(new GroqMessage("system", SYSTEM_PREAMBLE + "\n\nThong tin tham khao:\n" + simplifiedContext));

        int from = Math.max(0, history.size() - 1 - (maxHistoryTurns * 2));
        for (int i = from; i < history.size(); i++) {
            ChatMessage m = history.get(i);
            String role = switch (m.getSender()) {
                case USER -> "user";
                case BOT -> "assistant";
                case ADMIN -> "assistant"; // admin takeover messages still read as "assistant" to the model
            };
            messages.add(new GroqMessage(role, m.getContent()));
        }
        return messages;
    }
}
