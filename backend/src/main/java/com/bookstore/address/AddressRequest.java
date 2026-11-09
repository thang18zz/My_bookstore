package com.bookstore.address;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddressRequest {
    @NotBlank private String recipientName;
    @NotBlank @Email private String email;
    @NotBlank private String phone;
    @NotBlank private String line1;
    @NotBlank private String district;
    private String city;
    private boolean isDefault;
}
