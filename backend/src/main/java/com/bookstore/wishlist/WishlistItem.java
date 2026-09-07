package com.bookstore.wishlist;

import com.bookstore.auth.User;
import com.bookstore.common.BaseEntity;
import com.bookstore.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Backs Epic 2.3 - Danh sách yêu thích (UC2.3.1-3). */
@Getter @Setter
@Entity
@Table(name = "wishlist_items", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class WishlistItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id")
    private Product product;
}
