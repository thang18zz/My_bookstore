package com.bookstore.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewRequest {
    @NotNull private Long productId;
    @Min(1) @Max(5) private int rating;
    private String comment;
}
