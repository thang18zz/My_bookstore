package com.bookstore.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

/** UC3.1.1 view + UC3.1.6 total, returned together so the UI never has to make two calls. */
@Getter @AllArgsConstructor
public class CartDto {
    private List<CartItemDto> items;
    private BigDecimal total; // UC3.1.6
}
