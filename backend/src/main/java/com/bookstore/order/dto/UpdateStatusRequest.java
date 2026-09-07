package com.bookstore.order.dto;

import jakarta.validation.constraints.NotNull;
import com.bookstore.order.OrderStatus;
import lombok.Getter;
import lombok.Setter;

/** UC5.2.3 - Cập nhật trạng thái đơn hàng. */
@Getter @Setter
public class UpdateStatusRequest {
    @NotNull private OrderStatus status;
}
