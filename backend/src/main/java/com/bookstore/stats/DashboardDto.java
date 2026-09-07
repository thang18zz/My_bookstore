package com.bookstore.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

/** UC12.1.1-5 combined into a single dashboard payload. */
@Getter @AllArgsConstructor
public class DashboardDto {
    private long totalProducts;   // UC12.1.2
    private long totalCustomers;  // UC12.1.3
    private long totalOrders;     // UC12.1.4
    private BigDecimal revenue;   // UC12.1.5
}
