package com.bookstore.order;

import com.bookstore.address.Address;
import com.bookstore.auth.User;
import com.bookstore.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Backs Theme 4 (checkout/create) and Theme 5 (customer + admin manage order).
 * This is the
 * central record the chatbot's DataSimplifier reads from for UC14.1.3 (tra cứu
 * đơn hàng).
 */
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address shippingAddress; // UC4.1.2

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.OFFLINE;

    @Column(nullable = true)
    private String paymentReference;

    @Column(nullable = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(nullable = true)
    private LocalDateTime paidAt;

    @Column(nullable = false)
    private BigDecimal totalAmount; // UC4.2.2

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>(); // UC4.2.3

    void UpdatePaidAt() {
        paidAt = LocalDateTime.now();
    }
}
