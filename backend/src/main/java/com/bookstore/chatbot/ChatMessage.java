package com.bookstore.chatbot;

import com.bookstore.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** One turn of the conversation. rating is set later by UC14.1.6 and only applies to BOT messages. */
@Getter @Setter
@Entity
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id")
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType sender;

    @Column(length = 4000, nullable = false)
    private String content;

    private Integer rating; // 1-5, UC14.1.6, nullable until rated
}
