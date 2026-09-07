package com.bookstore.auth;

import com.bookstore.auth.dto.*;
import com.bookstore.common.BadRequestException;
import com.bookstore.common.JwtUtil;
import com.bookstore.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Implements UC1.1.1-3 and UC1.2.1-2. Other modules (cart, order, wishlist, chatbot) depend on the
 *  User entity this service manages, which is how "accounts" link the whole system together. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // UC1.1.1 - Đăng ký tài khoản
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return new AuthResponse(token, user.getRole().name(), user.getId(), user.getFullName());
    }

    // UC1.1.2 - Đăng nhập (Khách hàng hoặc Admin)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!user.isActive() || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return new AuthResponse(token, user.getRole().name(), user.getId(), user.getFullName());
    }

    // UC1.1.3 - Đăng xuất: JWT is stateless, so logout is a client-side token discard.
    // Exposed as an endpoint anyway so the frontend has one consistent call to make.
    public void logout() {
        // If a blacklist/refresh-token store is added later, invalidate it here.
    }

    // UC1.2.1 - Xem thông tin cá nhân
    public UserProfileDto getProfile(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserProfileDto(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole().name());
    }

    // UC1.2.2 - Cập nhật thông tin cá nhân
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (req.getFullName() != null) u.setFullName(req.getFullName());
        if (req.getPhone() != null) u.setPhone(req.getPhone());
        userRepository.save(u);
        return new UserProfileDto(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole().name());
    }
}
