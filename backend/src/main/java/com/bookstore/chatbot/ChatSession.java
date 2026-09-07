package com.bookstore.chatbot;

import com.bookstore.auth.User;
import com.bookstore.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

/** Backs UC14.1.1 (start) and is the root object the whole Theme 14 module revolves around.
 *  user is nullable so guests can chat before logging in (still get product info, just no order lookup). */
@Getter @Setter
@Entity
@Table(name = "chat_sessions")
public class ChatSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatSessionStatus status = ChatSessionStatus.BOT;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();
}
