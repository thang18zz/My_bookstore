package com.bookstore.review;

import com.bookstore.auth.User;
import com.bookstore.common.BaseEntity;
import com.bookstore.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Backs Epic 7.1 (customer reviews) and Epic 7.2 (admin moderation). */
@Getter @Setter
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(length = 1000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.VISIBLE;
}
