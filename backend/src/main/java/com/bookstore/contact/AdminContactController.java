package com.bookstore.contact;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/** UC13.1.3-4 - Admin xem/xử lý yêu cầu liên hệ. */
@RestController
@RequestMapping("/api/admin/contact")
@RequiredArgsConstructor
public class AdminContactController {

    private final ContactRepository contactRepository;

    @GetMapping // UC13.1.3
    public ApiResponse<Page<ContactRequest>> list(@RequestParam(required = false) ContactStatus status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        Page<ContactRequest> result = status != null
                ? contactRepository.findByStatus(status, PageRequest.of(page, size))
                : contactRepository.findAll(PageRequest.of(page, size));
        return ApiResponse.ok(result);
    }

    @PatchMapping("/{id}/reply") // UC13.1.4 - Xử lý yêu cầu liên hệ
    public ApiResponse<ContactRequest> reply(@PathVariable Long id, @RequestParam String reply) {
        ContactRequest cr = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact request not found"));
        cr.setReply(reply);
        cr.setStatus(ContactStatus.RESOLVED);
        return ApiResponse.ok("Replied", contactRepository.save(cr));
    }
}
