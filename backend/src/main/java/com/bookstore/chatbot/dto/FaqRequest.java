package com.bookstore.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FaqRequest {
    @NotBlank private String question;
    @NotBlank private String answer;
    private String keywords;
}
