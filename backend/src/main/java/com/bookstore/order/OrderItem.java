package com.bookstore.order;

import com.bookstore.common.BaseEntity;
import com.bookstore.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/** UC4.2.3 - Ghi nhận sản phẩm trong đơn hàng. Price is snapshotted at order time so later price
 *  changes (UC8.1.6) never rewrite historical orders. */
@Getter @Setter
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal priceAtPurchase;
}
