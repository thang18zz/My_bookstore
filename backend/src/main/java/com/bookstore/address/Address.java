package com.bookstore.address;

import com.bookstore.auth.User;
import com.bookstore.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Backs Theme 6. Referenced by order.Order for UC4.1.2 (chọn địa chỉ giao hàng). */
@Getter @Setter
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String line1;

    private String city;

    @Column(nullable = false)
    private boolean isDefault = false;
}
