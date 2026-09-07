package com.bookstore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Shared response for UC5.1.2 (customer) and UC5.2.2 (admin) - same order, same shape. */
@Getter @AllArgsConstructor
public class OrderDto {
    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String recipientName;
    private String shippingAddress;
    private List<OrderItemDto> items;
}
