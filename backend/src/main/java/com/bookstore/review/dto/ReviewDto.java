package com.bookstore.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

/** UC7.1.1-4 / UC7.2.1-4 - flat DTO so Jackson never touches the lazy User/Product proxies on
 *  Review directly (that was throwing a 500 ByteBuddyInterceptor serialization error). */
@Getter
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private String status;
    private LocalDateTime createdAt;
}
