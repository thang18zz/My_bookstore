package com.bookstore.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ContactRequestDto {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank private String message;
}
