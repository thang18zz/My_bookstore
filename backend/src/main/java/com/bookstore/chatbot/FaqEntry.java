package com.bookstore.chatbot;

import com.bookstore.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Admin-curated knowledge base entries, injected into the Groq system prompt as extra context.
 *  Backs UC14.2.2 (kịch bản/FAQ) and UC14.2.5 (cập nhật dữ liệu tri thức). */
@Getter @Setter
@Entity
@Table(name = "faq_entries")
public class FaqEntry extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false, length = 2000)
    private String answer;

    @Column(length = 300)
    private String keywords; // comma-separated, used for lightweight keyword matching before sending to Groq

    @Column(nullable = false)
    private boolean active = true;
}
