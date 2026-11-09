package com.bookstore.address;

import com.bookstore.auth.User;
import com.bookstore.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Backs Theme 6. Referenced by order.Order for UC4.1.2 (chọn địa chỉ giao hàng). */
@Getter @Setter
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false, columnDefinition = "varchar(255) default ''")
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String line1;

    @Column(nullable = false)
    private String district;

    private String city;

    @JsonProperty("default")
    @Column(nullable = false)
    private boolean isDefault = false;

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @JsonProperty("isDefault")
    public boolean getIsDefault() {
        return isDefault;
    }
}
