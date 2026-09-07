package com.bookstore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter @AllArgsConstructor
public class OrderItemDto {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
}
