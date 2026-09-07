package com.bookstore.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter @AllArgsConstructor
public class CartItemDto {
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private Integer availableStock; // supports UC3.2.1/UC3.2.2 checks in the UI
    private BigDecimal subtotal;
}
