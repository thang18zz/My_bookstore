package com.bookstore.product;

import com.bookstore.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Backs Theme 9 (Admin category CRUD) and is referenced by Product for UC2.1.2 / UC2.2.2. */
@Getter @Setter
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;
    private String description;
}
