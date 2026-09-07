package com.bookstore.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class BestSellerDto {
    private Long productId;
    private String productName;
    private long unitsSold;
}
