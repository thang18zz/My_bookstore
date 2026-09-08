package com.bookstore.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

/** Full product view for UC2.1.3 (chi tiết), also carries price (UC2.1.4) and stock (UC2.1.5). */
@Getter @AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private boolean inStock;
    private boolean active;
}
