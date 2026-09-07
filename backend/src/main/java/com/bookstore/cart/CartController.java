package com.bookstore.cart;

import com.bookstore.cart.dto.*;
import com.bookstore.common.ApiResponse;
import com.bookstore.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping // UC3.1.1 / UC3.1.6
    public ApiResponse<CartDto> view() {
        return ApiResponse.ok(cartService.view(CurrentUser.id()));
    }

    @PostMapping("/items") // UC3.1.2 (validates via UC3.2.1/UC3.2.2)
    public ApiResponse<CartDto> add(@Valid @RequestBody AddCartItemRequest req) {
        return ApiResponse.ok("Added to cart", cartService.addItem(CurrentUser.id(), req));
    }

    @PutMapping("/items/{productId}") // UC3.1.3
    public ApiResponse<CartDto> update(@PathVariable Long productId, @Valid @RequestBody UpdateCartItemRequest req) {
        return ApiResponse.ok(cartService.updateItem(CurrentUser.id(), productId, req));
    }

    @DeleteMapping("/items/{productId}") // UC3.1.4
    public ApiResponse<CartDto> remove(@PathVariable Long productId) {
        return ApiResponse.ok(cartService.removeItem(CurrentUser.id(), productId));
    }

    @DeleteMapping // UC3.1.5
    public ApiResponse<Void> clear() {
        cartService.clear(CurrentUser.id());
        return ApiResponse.ok("Cart cleared", null);
    }
}
