package com.bookstore.chatbot;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FaqRepository extends JpaRepository<FaqEntry, Long> {
    List<FaqEntry> findByActiveTrue();
}
