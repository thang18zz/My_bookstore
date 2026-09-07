package com.bookstore.chatbot;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** One entry of the "messages" array in the Groq /chat/completions request body. */
@Getter @AllArgsConstructor
public class GroqMessage {
    private String role;    // "system" | "user" | "assistant"
    private String content;
}
