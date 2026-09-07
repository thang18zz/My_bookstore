package com.bookstore.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable); // UC7.1.1
    Optional<Review> findByIdAndUserId(Long id, Long userId); // ownership check for UC7.1.3/7.1.4
    Page<Review> findAll(Pageable pageable); // UC7.2.1
}
