package com.bookstore.product;

import com.bookstore.common.BadRequestException;
import com.bookstore.common.ResourceNotFoundException;
import com.bookstore.product.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

/** Customer-facing catalog logic: Theme 2 (browse/search) plus the read half of Theme 8 (admin views
 *  the same entity through AdminProductController below). This is the module the chatbot's
 *  DataSimplifier pulls product facts from for UC14.1.2 / UC14.1.4. */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // UC2.1.1 - Xem danh sách sản phẩm
    public Page<ProductSummaryDto> list(int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return productRepository.findByActiveTrue(pageable).map(this::toSummary);
    }

    // UC2.1.2 / UC2.2.2 - Xem/lọc sản phẩm theo danh mục
    public Page<ProductSummaryDto> listByCategory(Long categoryId, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return productRepository.findByActiveTrueAndCategoryId(categoryId, pageable).map(this::toSummary);
    }

    // UC2.2.1 - Tìm kiếm sản phẩm theo từ khóa
    public Page<ProductSummaryDto> search(String keyword, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return productRepository.findByActiveTrueAndNameContainingIgnoreCase(keyword, pageable)
                .map(this::toSummary);
    }

    // UC2.1.3 / UC2.1.4 / UC2.1.5 - Chi tiết, giá, tồn kho (một chi tiết sản phẩm trả về cả ba)
    public ProductDto getDetail(Long id) {
        Product p = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return toDto(p);
    }

    // UC2.2.3 - Sắp xếp sản phẩm: "price_asc" | "price_desc" | "newest" (default)
    private Pageable buildPageable(int page, int size, String sort) {
        Sort s = switch (sort == null ? "newest" : sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name" -> Sort.by("name").ascending();
            default -> Sort.by("createdAt").descending();
        };
        return PageRequest.of(page, size, s);
    }

    private ProductSummaryDto toSummary(Product p) {
        return new ProductSummaryDto(p.getId(), p.getName(), p.getPrice(), p.getImageUrl(), p.getStockQuantity() > 0);
    }

    ProductDto toDto(Product p) {
        return new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStockQuantity(),
                p.getImageUrl(), p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null, p.getStockQuantity() > 0, p.isActive());
    }

    Product requireEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    Category requireCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
