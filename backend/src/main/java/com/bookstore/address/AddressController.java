package com.bookstore.address;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping // UC6.1.1
    public ApiResponse<List<Address>> list() {
        return ApiResponse.ok(addressService.list(CurrentUser.id()));
    }

    @PostMapping // UC6.1.2
    public ApiResponse<Address> add(@Valid @RequestBody AddressRequest req) {
        return ApiResponse.ok("Address added", addressService.add(CurrentUser.id(), req));
    }

    @PutMapping("/{id}") // UC6.1.3
    public ApiResponse<Address> update(@PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        return ApiResponse.ok("Address updated", addressService.update(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/{id}") // UC6.1.4
    public ApiResponse<Void> delete(@PathVariable Long id) {
        addressService.delete(CurrentUser.id(), id);
        return ApiResponse.ok("Address deleted", null);
    }

    @PatchMapping("/{id}/default") // UC6.1.5
    public ApiResponse<Address> setDefault(@PathVariable Long id) {
        return ApiResponse.ok(addressService.setDefault(CurrentUser.id(), id));
    }
}
