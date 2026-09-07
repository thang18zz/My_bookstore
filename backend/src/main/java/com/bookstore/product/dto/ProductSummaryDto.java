package com.bookstore.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

/** Lightweight card view for list/search/sort endpoints (UC2.1.1, UC2.2.1-3). */
@Getter @AllArgsConstructor
public class ProductSummaryDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private boolean inStock;
}
