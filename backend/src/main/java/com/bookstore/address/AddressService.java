package com.bookstore.address;

import com.bookstore.auth.User;
import com.bookstore.auth.UserRepository;
import com.bookstore.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/** Implements Epic 6.1 (UC6.1.1-5). order.OrderService calls requireOwned() during checkout (UC4.1.2). */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // UC6.1.1 - Xem danh sách địa chỉ
    public List<Address> list(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    // UC6.1.2 - Thêm địa chỉ giao hàng
    public Address add(Long userId, AddressRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Address a = new Address();
        a.setUser(user);
        applyRequest(a, req, userId);
        return addressRepository.save(a);
    }

    // UC6.1.3 - Cập nhật địa chỉ giao hàng
    public Address update(Long userId, Long addressId, AddressRequest req) {
        Address a = requireOwned(userId, addressId);
        applyRequest(a, req, userId);
        return addressRepository.save(a);
    }

    // UC6.1.4 - Xóa địa chỉ giao hàng
    public void delete(Long userId, Long addressId) {
        Address a = requireOwned(userId, addressId);
        addressRepository.delete(a);
    }

    // UC6.1.5 - Chọn địa chỉ mặc định
    public Address setDefault(Long userId, Long addressId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(prev -> {
            prev.setDefault(false);
            addressRepository.save(prev);
        });
        Address a = requireOwned(userId, addressId);
        a.setDefault(true);
        return addressRepository.save(a);
    }

    public Address requireOwned(Long userId, Long addressId) {
        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!a.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Address not found");
        }
        return a;
    }

    private void applyRequest(Address a, AddressRequest req, Long userId) {
        a.setRecipientName(req.getRecipientName());
        a.setPhone(req.getPhone());
        a.setLine1(req.getLine1());
        a.setCity(req.getCity());
        if (req.isDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(prev -> {
                prev.setDefault(false);
                addressRepository.save(prev);
            });
        }
        a.setDefault(req.isDefault());
    }
}
