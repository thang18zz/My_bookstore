package com.bookstore.chatbot;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
    long countBySenderAndRatingIsNotNull(SenderType sender); // UC14.2.4 - avg rating support
    List<ChatMessage> findBySenderAndRatingIsNotNull(SenderType sender);
}
