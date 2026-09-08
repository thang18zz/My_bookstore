package com.bookstore.product;

import com.bookstore.common.ApiResponse;
import com.bookstore.product.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/** Theme 8 - Quản lý sản phẩm (Admin). Reuses ProductService for read paths (UC8.1.1/8.1.2)
 *  so admin and customer never drift into two different views of the same data. */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping // UC8.1.1
    public ApiResponse<Page<ProductDto>> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(productRepository.findAll(PageRequest.of(page, size)).map(productService::toDto));
    }

    @GetMapping("/{id}") // UC8.1.2
    public ApiResponse<ProductDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(productService.toDto(productService.requireEntity(id)));
    }

    @PostMapping // UC8.1.3 - Thêm sản phẩm
    public ApiResponse<ProductDto> create(@Valid @RequestBody ProductRequest req) {
        Product p = new Product();
        applyRequest(p, req);
        return ApiResponse.ok("Product created", productService.toDto(productRepository.save(p)));
    }

    @PutMapping("/{id}") // UC8.1.4 / UC8.1.7 - Cập nhật sản phẩm / thông tin sản phẩm
    public ApiResponse<ProductDto> update(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        Product p = productService.requireEntity(id);
        applyRequest(p, req);
        return ApiResponse.ok("Product updated", productService.toDto(productRepository.save(p)));
    }

    @PatchMapping("/{id}/price") // UC8.1.6 - Cập nhật giá sản phẩm
    public ApiResponse<ProductDto> updatePrice(@PathVariable Long id, @RequestParam java.math.BigDecimal price) {
        Product p = productService.requireEntity(id);
        p.setPrice(price);
        return ApiResponse.ok("Price updated", productService.toDto(productRepository.save(p)));
    }

    @DeleteMapping("/{id}") // UC8.1.5 - Xóa sản phẩm: mặc định xóa mềm, hỗ trợ ?hard=true để xóa cứng
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean hard) {
        Product p = productService.requireEntity(id);
        if (hard) {
            try {
                productRepository.delete(p);
                return ApiResponse.ok("Đã xóa vĩnh viễn sản phẩm khỏi hệ thống", null);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                throw new com.bookstore.common.BadRequestException(
                        "Không thể xóa cứng: Sản phẩm đã tồn tại trong lịch sử đơn hàng hoặc giỏ hàng. Vui lòng sử dụng Xóa mềm để bảo toàn dữ liệu.");
            }
        } else {
            p.setActive(false);
            productRepository.save(p);
            return ApiResponse.ok("Đã xóa mềm (ẩn sản phẩm khỏi cửa hàng)", null);
        }
    }

    @PatchMapping("/{id}/restore") // Khôi phục sản phẩm đã xóa mềm
    public ApiResponse<ProductDto> restore(@PathVariable Long id) {
        Product p = productService.requireEntity(id);
        p.setActive(true);
        return ApiResponse.ok("Đã khôi phục sản phẩm thành công", productService.toDto(productRepository.save(p)));
    }

    private void applyRequest(Product p, ProductRequest req) {
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStockQuantity(req.getStockQuantity());
        p.setImageUrl(req.getImageUrl());
        p.setCategory(categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new com.bookstore.common.ResourceNotFoundException("Category not found")));
        p.setActive(true);
    }
}
