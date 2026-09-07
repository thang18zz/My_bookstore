package com.bookstore.inventory;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.ResourceNotFoundException;
import com.bookstore.product.Product;
import com.bookstore.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/** Theme 11 - Quản lý tồn kho (Admin): UC11.1.1-5. Reads/writes Product.stockQuantity directly -
 *  the same field Theme 3 checks before a customer can add to cart and Theme 4 decrements at checkout. */
@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;

    @GetMapping // UC11.1.1
    public ApiResponse<Page<InventoryDto>> list(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(productRepository.findAll(PageRequest.of(page, size)).map(this::toDto));
    }

    @GetMapping("/{productId}") // UC11.1.2 - Tra cứu tồn kho
    public ApiResponse<InventoryDto> get(@PathVariable Long productId) {
        return ApiResponse.ok(toDto(requireProduct(productId)));
    }

    @PatchMapping("/{productId}") // UC11.1.3 - Cập nhật số lượng tồn kho
    public ApiResponse<InventoryDto> setQuantity(@PathVariable Long productId, @RequestParam int quantity) {
        Product p = requireProduct(productId);
        p.setStockQuantity(quantity);
        return ApiResponse.ok("Stock updated", toDto(productRepository.save(p)));
    }

    @GetMapping("/low-stock") // UC11.1.4 - Theo dõi sản phẩm sắp hết hàng
    public ApiResponse<Page<InventoryDto>> lowStock(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(productRepository
                .findByStockQuantityLessThanEqual(LOW_STOCK_THRESHOLD, PageRequest.of(page, size))
                .map(this::toDto));
    }

    @PatchMapping("/{productId}/restock") // UC11.1.5 - Cập nhật tồn kho khi nhập hàng
    public ApiResponse<InventoryDto> restock(@PathVariable Long productId, @RequestParam int addedQuantity) {
        Product p = requireProduct(productId);
        p.setStockQuantity(p.getStockQuantity() + addedQuantity);
        return ApiResponse.ok("Restocked", toDto(productRepository.save(p)));
    }

    private Product requireProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private InventoryDto toDto(Product p) {
        return new InventoryDto(p.getId(), p.getName(), p.getStockQuantity(), p.getStockQuantity() <= LOW_STOCK_THRESHOLD);
    }
}
