package com.bookstore.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    // UC10.1.6 - Admin tra cứu khách hàng
    Page<User> findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
            Role r1, String name, Role r2, String email, Pageable pageable);
}
