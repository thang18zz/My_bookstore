package com.bookstore.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** UC4.1.1/4.1.2 - Tiến hành thanh toán + chọn địa chỉ giao hàng. */
@Getter @Setter
public class CheckoutRequest {
    @NotNull private Long addressId;
}
