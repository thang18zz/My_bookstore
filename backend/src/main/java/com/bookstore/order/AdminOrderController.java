package com.bookstore.order;

import com.bookstore.common.ApiResponse;
import com.bookstore.order.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/** Epic 5.2 - Admin quản lý đơn hàng: UC5.2.1-5. */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @GetMapping // UC5.2.1 (and UC5.2.5 tra cứu when ?status= is passed)
    public ApiResponse<Page<OrderDto>> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status) {
        Page<Order> orders = status != null
                ? orderRepository.findByOrderStatus(status, PageRequest.of(page, size))
                : orderRepository.findAll(PageRequest.of(page, size));
        return ApiResponse.ok(orders.map(orderService::toDto));
    }

    @GetMapping("/{id}") // UC5.2.2
    public ApiResponse<OrderDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.toDto(orderService.requireEntity(id)));
    }

    @PatchMapping("/{id}/status") // UC5.2.3 / UC5.2.4 - Cập nhật trạng thái / xử lý đơn hàng
    public ApiResponse<OrderDto> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest req) {
        Order o = orderService.requireEntity(id);
        o.setOrderStatus(req.getStatus());
        return ApiResponse.ok("Order status updated", orderService.toDto(orderRepository.save(o)));
    }
}
