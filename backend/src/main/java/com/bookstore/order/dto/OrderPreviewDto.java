package com.bookstore.order.dto;

import com.bookstore.cart.dto.CartDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** UC4.1.3 - Kiểm tra thông tin đơn hàng trước khi xác nhận. */
@Getter @AllArgsConstructor
public class OrderPreviewDto {
    private CartDto cart;
    private Long addressId;
    private String addressSummary;
}
