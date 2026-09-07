package com.bookstore.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** UC1.1.2 - Đăng nhập */
@Getter @Setter
public class LoginRequest {
    @NotBlank private String email;
    @NotBlank private String password;
}
