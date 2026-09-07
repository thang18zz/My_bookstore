package com.bookstore.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrue(Pageable pageable); // UC2.1.1

    Page<Product> findByActiveTrueAndCategoryId(Long categoryId, Pageable pageable); // UC2.1.2 / UC2.2.2

    Page<Product> findByActiveTrueAndNameContainingIgnoreCase(String keyword, Pageable pageable); // UC2.2.1

    // UC11.1.4 - Theo dõi sản phẩm sắp hết hàng
    Page<Product> findByStockQuantityLessThanEqual(int threshold, Pageable pageable);
}
