package com.bookstore.order;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.CurrentUser;
import com.bookstore.order.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Customer-facing: Theme 4 (checkout) + Epic 5.1 (order history/detail/cancel).
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/preview") // UC4.1.3
    public ApiResponse<OrderPreviewDto> preview(@RequestParam Long addressId) {
        return ApiResponse.ok(orderService.preview(CurrentUser.id(), addressId));
    }

    @PostMapping("/checkout") // UC4.1.1 / UC4.1.4 / UC4.2.1-4
    public ApiResponse<OrderDto> checkout(@Valid @RequestBody CheckoutRequest req) {
        return ApiResponse.ok("Order placed", orderService.checkout(CurrentUser.id(), req));
    }

    @GetMapping // UC5.1.1
    public ApiResponse<List<OrderDto>> myOrders() {
        return ApiResponse.ok(orderService.myOrders(CurrentUser.id()));
    }

    @GetMapping("/{id}") // UC5.1.2 / UC5.1.3 (status is part of OrderDto, poll this to "track")
    public ApiResponse<OrderDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.myOrderDetail(CurrentUser.id(), id));
    }

    @PostMapping("/{id}/cancel") // UC5.1.4
    public ApiResponse<OrderDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok("Order cancelled", orderService.cancel(CurrentUser.id(), id));
    }

}
