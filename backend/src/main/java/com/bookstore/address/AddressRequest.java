package com.bookstore.address;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddressRequest {
    @NotBlank private String recipientName;
    @NotBlank private String phone;
    @NotBlank private String line1;
    private String city;
    private boolean isDefault;
}
