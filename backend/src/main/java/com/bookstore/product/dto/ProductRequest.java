package com.bookstore.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/** UC8.1.3 (create) and UC8.1.4/8.1.7 (update) share this request shape. */
@Getter @Setter
public class ProductRequest {
    @NotBlank private String name;
    private String description;
    @NotNull private BigDecimal price;
    @NotNull private Integer stockQuantity;
    private String imageUrl;
    @NotNull private Long categoryId;
}
