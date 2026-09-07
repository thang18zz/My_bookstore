package com.bookstore.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class InventoryDto {
    private Long productId;
    private String productName;
    private Integer stockQuantity;
    private boolean lowStock;
}
