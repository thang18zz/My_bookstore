package com.bookstore.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** UC14.1.1 - wraps the new session id in a named field instead of returning a bare number,
 *  which was breaking automated tests expecting `data.sessionId`. */
@Getter
@AllArgsConstructor
public class StartSessionResponse {
    private Long sessionId;
}
