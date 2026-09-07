package com.bookstore.cart;

import com.bookstore.common.BaseEntity;
import com.bookstore.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "cart_items")
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}
