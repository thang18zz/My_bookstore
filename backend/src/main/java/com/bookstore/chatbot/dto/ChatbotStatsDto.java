package com.bookstore.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** UC14.2.4 - Xem thống kê hiệu quả chatbot */
@Getter @AllArgsConstructor
public class ChatbotStatsDto {
    private long totalSessions;
    private long handedOffSessions;
    private double handoffRatePercent;
    private double averageRating;
}
