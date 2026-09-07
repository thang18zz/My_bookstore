package com.bookstore.chatbot;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findByIdAndUserId(Long id, Long userId);
    Page<ChatSession> findByStatus(ChatSessionStatus status, Pageable pageable); // UC14.2.3 handoff queue
    Page<ChatSession> findAll(Pageable pageable); // UC14.2.1
    long countByStatus(ChatSessionStatus status); // UC14.2.4
}
