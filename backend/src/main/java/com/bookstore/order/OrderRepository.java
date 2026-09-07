package com.bookstore.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId); // UC5.1.1
    Optional<Order> findByIdAndUserId(Long id, Long userId);   // UC5.1.2 ownership check
    Page<Order> findAll(Pageable pageable);                    // UC5.2.1
    Page<Order> findByStatus(OrderStatus status, Pageable pageable); // UC5.2.5 tra cứu theo trạng thái
    long countByStatus(OrderStatus status);                    // UC12.1.4 / UC12.2.x
}
