package com.bookstore.review;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.ResourceNotFoundException;
import com.bookstore.review.dto.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/** Epic 7.2 - Admin quản lý đánh giá: UC7.2.1-4. */
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    @GetMapping // UC7.2.1
    public ApiResponse<Page<ReviewDto>> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(reviewRepository.findAll(PageRequest.of(page, size)).map(reviewService::toDto));
    }

    @GetMapping("/{id}") // UC7.2.2
    public ApiResponse<ReviewDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(reviewService.toDto(reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"))));
    }

    @DeleteMapping("/{id}") // UC7.2.3
    public ApiResponse<Void> delete(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return ApiResponse.ok("Review deleted", null);
    }

    @PatchMapping("/{id}/hide") // UC7.2.4 - Quản lý đánh giá vi phạm
    public ApiResponse<ReviewDto> hide(@PathVariable Long id) {
        Review r = reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        r.setStatus(ReviewStatus.HIDDEN);
        return ApiResponse.ok("Review hidden", reviewService.toDto(reviewRepository.save(r)));
    }
}
