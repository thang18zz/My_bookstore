package com.bookstore.review;

import com.bookstore.auth.User;
import com.bookstore.auth.UserRepository;
import com.bookstore.common.BadRequestException;
import com.bookstore.common.ResourceNotFoundException;
import com.bookstore.product.Product;
import com.bookstore.product.ProductRepository;
import com.bookstore.review.dto.ReviewDto;
import com.bookstore.review.dto.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Implements Epic 7.1: UC7.1.1-4. Admin moderation (Epic 7.2) lives in AdminReviewController,
 *  reusing this repository directly since there is no separate write-path for admins to duplicate. */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // UC7.1.1 - Xem đánh giá sản phẩm
    public Page<ReviewDto> listForProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.VISIBLE, pageable).map(this::toDto);
    }

    // UC7.1.2 - Đánh giá sản phẩm
    public ReviewDto add(Long userId, ReviewRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Review r = new Review();
        r.setUser(user);
        r.setProduct(product);
        r.setRating(req.getRating());
        r.setComment(req.getComment());
        return toDto(reviewRepository.save(r));
    }

    // UC7.1.3 - Chỉnh sửa đánh giá
    public ReviewDto edit(Long userId, Long reviewId, ReviewRequest req) {
        Review r = requireOwned(userId, reviewId);
        r.setRating(req.getRating());
        r.setComment(req.getComment());
        return toDto(reviewRepository.save(r));
    }

    // Flattens the entity so Jackson never touches the lazy User/Product proxies directly
    // (that was the cause of the 500 "ByteBuddyInterceptor" serialization error).
    public ReviewDto toDto(Review r) {
        return new ReviewDto(r.getId(), r.getProduct().getId(), r.getProduct().getName(),
                r.getUser().getId(), r.getUser().getFullName(), r.getRating(), r.getComment(),
                r.getStatus().name(), r.getCreatedAt());
    }

    // UC7.1.4 - Xóa đánh giá của mình
    public void deleteOwn(Long userId, Long reviewId) {
        reviewRepository.delete(requireOwned(userId, reviewId));
    }

    private Review requireOwned(Long userId, Long reviewId) {
        Review r = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (r.getStatus() == ReviewStatus.HIDDEN) {
            throw new BadRequestException("This review was moderated and can no longer be edited");
        }
        return r;
    }
}
