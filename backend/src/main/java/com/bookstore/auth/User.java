package com.bookstore.auth;

import com.bookstore.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Backs UC1.1.1-3 (register/login/logout) and UC1.2.1-3 (profile view/update, admin account mgmt). */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hash - never returned in any DTO

    @Column(nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    private boolean active = true;
}
