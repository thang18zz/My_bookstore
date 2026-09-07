package com.bookstore.contact;

import com.bookstore.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Public endpoints: UC13.1.1 (static info, hardcoded here for simplicity) and UC13.1.2 (submit). */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactRepository contactRepository;

    @GetMapping("/info") // UC13.1.1 - Xem thông tin liên hệ
    public ApiResponse<Object> info() {
        return ApiResponse.ok(new Object() {
            public final String email = "support@bookstore.vn";
            public final String phone = "1900-1234";
            public final String address = "123 Book Street, Hanoi";
        });
    }

    @PostMapping // UC13.1.2 - Gửi yêu cầu liên hệ
    public ApiResponse<ContactRequest> submit(@Valid @RequestBody ContactRequestDto dto) {
        ContactRequest cr = new ContactRequest();
        cr.setName(dto.getName());
        cr.setEmail(dto.getEmail());
        cr.setMessage(dto.getMessage());
        return ApiResponse.ok("Contact request submitted", contactRepository.save(cr));
    }
}
