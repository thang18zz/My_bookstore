package com.bookstore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** UC1.1.1 - Đăng ký tài khoản */
@Getter @Setter
public class RegisterRequest {
    @NotBlank private String fullName;
    @Email @NotBlank private String email;
    @Size(min = 6, message = "Password must be at least 6 characters") private String password;
    private String phone;
}
