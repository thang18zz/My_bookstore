package com.bookstore.contact;

import com.bookstore.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Backs Epic 13.1: UC13.1.1-4. Deliberately independent of User (a guest can contact the store too). */
@Getter @Setter
@Entity
@Table(name = "contact_requests")
public class ContactRequest extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(length = 2000, nullable = false)
    private String message;

    @Column(length = 2000)
    private String reply;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactStatus status = ContactStatus.PENDING;
}
