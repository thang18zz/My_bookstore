package com.bookstore.product;

import com.bookstore.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/** Central entity referenced by wishlist, cart, order-item, review and the chatbot's DataSimplifier.
 *  Backs Theme 2 (browse/search) and Theme 8 (admin CRUD). */
@Getter @Setter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price; // UC2.1.4 / UC8.1.6

    @Column(nullable = false)
    private Integer stockQuantity = 0; // UC2.1.5 / UC11.x

    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // UC2.1.2
}
