package com.bookstore.review;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.CurrentUser;
import com.bookstore.review.dto.ReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}") // UC7.1.1
    public ApiResponse<Page<Review>> forProduct(@PathVariable Long productId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(reviewService.listForProduct(productId, PageRequest.of(page, size)));
    }

    @PostMapping // UC7.1.2
    public ApiResponse<Review> add(@Valid @RequestBody ReviewRequest req) {
        return ApiResponse.ok("Review submitted", reviewService.add(CurrentUser.id(), req));
    }

    @PutMapping("/{id}") // UC7.1.3
    public ApiResponse<Review> edit(@PathVariable Long id, @Valid @RequestBody ReviewRequest req) {
        return ApiResponse.ok("Review updated", reviewService.edit(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/{id}") // UC7.1.4
    public ApiResponse<Void> delete(@PathVariable Long id) {
        reviewService.deleteOwn(CurrentUser.id(), id);
        return ApiResponse.ok("Review deleted", null);
    }
}
