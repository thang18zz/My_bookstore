package com.bookstore.cart;

import com.bookstore.auth.User;
import com.bookstore.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

/** One cart per user. Backs Epic 3.1 / 3.2. Consumed by order.OrderService when checkout completes (UC4.2.1). */
@Getter @Setter
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}
