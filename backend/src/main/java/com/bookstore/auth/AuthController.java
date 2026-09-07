package com.bookstore.auth;

import com.bookstore.auth.dto.*;
import com.bookstore.common.ApiResponse;
import com.bookstore.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register") // UC1.1.1
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok("Registered successfully", authService.register(req));
    }

    @PostMapping("/login") // UC1.1.2
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/logout") // UC1.1.3
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok("Logged out", null);
    }

    @GetMapping("/me") // UC1.2.1
    public ApiResponse<UserProfileDto> me() {
        return ApiResponse.ok(authService.getProfile(CurrentUser.id()));
    }

    @PutMapping("/me") // UC1.2.2
    public ApiResponse<UserProfileDto> updateMe(@RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok("Profile updated", authService.updateProfile(CurrentUser.id(), req));
    }
}
