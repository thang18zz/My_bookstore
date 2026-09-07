package com.bookstore.chatbot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/** UC14.1.6 - Đánh giá câu trả lời của chatbot */
@Getter @Setter
public class RateMessageRequest {
    @Min(1) @Max(5) private int rating;
}
