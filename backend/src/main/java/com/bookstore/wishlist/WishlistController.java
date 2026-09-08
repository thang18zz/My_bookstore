package com.bookstore.wishlist;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.CurrentUser;
import com.bookstore.wishlist.dto.WishlistItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping // UC2.3.1
    public ApiResponse<List<WishlistItemDto>> list() {
        return ApiResponse.ok(wishlistService.list(CurrentUser.id()));
    }

    @PostMapping("/{productId}") // UC2.3.2
    public ApiResponse<WishlistItemDto> add(@PathVariable Long productId) {
        return ApiResponse.ok("Added to wishlist", wishlistService.add(CurrentUser.id(), productId));
    }

    @DeleteMapping("/{productId}") // UC2.3.3
    public ApiResponse<Void> remove(@PathVariable Long productId) {
        wishlistService.remove(CurrentUser.id(), productId);
        return ApiResponse.ok("Removed from wishlist", null);
    }
}
