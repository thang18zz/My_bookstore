package com.bookstore.product;

import com.bookstore.common.ApiResponse;
import com.bookstore.product.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/** Public/customer endpoints - Theme 2 (Khám phá sản phẩm). No auth required (see SecurityConfig). */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping // UC2.1.1
    public ApiResponse<Page<ProductSummaryDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort) {
        return ApiResponse.ok(productService.list(page, size, sort));
    }

    @GetMapping("/category/{categoryId}") // UC2.1.2 / UC2.2.2
    public ApiResponse<Page<ProductSummaryDto>> byCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort) {
        return ApiResponse.ok(productService.listByCategory(categoryId, page, size, sort));
    }

    @GetMapping("/search") // UC2.2.1 (combine with UC2.2.3 via ?sort=)
    public ApiResponse<Page<ProductSummaryDto>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort) {
        return ApiResponse.ok(productService.search(keyword, page, size, sort));
    }

    @GetMapping("/{id}") // UC2.1.3 / UC2.1.4 / UC2.1.5
    public ApiResponse<ProductDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(productService.getDetail(id));
    }
}
