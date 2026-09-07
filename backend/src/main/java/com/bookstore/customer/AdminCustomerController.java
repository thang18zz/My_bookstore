package com.bookstore.customer;

import com.bookstore.auth.Role;
import com.bookstore.auth.User;
import com.bookstore.auth.UserRepository;
import com.bookstore.auth.dto.RegisterRequest;
import com.bookstore.common.ApiResponse;
import com.bookstore.common.BadRequestException;
import com.bookstore.common.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/** Theme 10 - Quản lý khách hàng (Admin): UC10.1.1-6. Operates on the same User entity as
 *  auth.AuthService, just scoped to Role.CUSTOMER and exposed only under /api/admin/**. */
@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping // UC10.1.1 (and UC10.1.6 tra cứu when ?q= is passed)
    public ApiResponse<Page<User>> list(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(required = false) String q) {
        String query = q == null ? "" : q;
        Page<User> result = userRepository
                .findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                        Role.CUSTOMER, query, Role.CUSTOMER, query, PageRequest.of(page, size));
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}") // UC10.1.2
    public ApiResponse<User> detail(@PathVariable Long id) {
        return ApiResponse.ok(requireCustomer(id));
    }

    @PostMapping // UC10.1.3 - Thêm khách hàng
    public ApiResponse<User> create(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) throw new BadRequestException("Email already exists");
        User u = new User();
        u.setEmail(req.getEmail());
        u.setFullName(req.getFullName());
        u.setPhone(req.getPhone());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.CUSTOMER);
        return ApiResponse.ok("Customer created", userRepository.save(u));
    }

    @PutMapping("/{id}") // UC10.1.4
    public ApiResponse<User> update(@PathVariable Long id, @RequestBody RegisterRequest req) {
        User u = requireCustomer(id);
        u.setFullName(req.getFullName());
        u.setPhone(req.getPhone());
        return ApiResponse.ok("Customer updated", userRepository.save(u));
    }

    @DeleteMapping("/{id}") // UC10.1.5
    public ApiResponse<Void> delete(@PathVariable Long id) {
        User u = requireCustomer(id);
        u.setActive(false); // soft-delete keeps order history intact
        userRepository.save(u);
        return ApiResponse.ok("Customer deactivated", null);
    }

    private User requireCustomer(Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (u.getRole() != Role.CUSTOMER) throw new ResourceNotFoundException("Customer not found");
        return u;
    }
}
