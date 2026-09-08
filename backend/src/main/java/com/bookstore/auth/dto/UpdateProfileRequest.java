package com.bookstore.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/** UC1.2.2 - Cập nhật thông tin cá nhân */
@Getter @Setter
public class UpdateProfileRequest {
    @NotBlank private String fullName;
    @Pattern(regexp = "^[0-9+ ()-]{8,15}$", message = "Invalid phone number")
    private String phone;
}
