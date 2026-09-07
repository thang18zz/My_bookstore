package com.bookstore.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private String sender; // USER | BOT | ADMIN
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
}
