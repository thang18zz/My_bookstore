package com.bookstore.auth.dto;

import lombok.Getter;
import lombok.Setter;

/** UC1.2.2 - Cập nhật thông tin cá nhân */
@Getter @Setter
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
}
