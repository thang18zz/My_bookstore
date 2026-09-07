package com.bookstore.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Returned by UC1.2.1; note there is deliberately no password field. */
@Getter @AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
}
