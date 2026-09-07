package com.bookstore.wishlist;

import com.bookstore.auth.User;
import com.bookstore.auth.UserRepository;
import com.bookstore.common.BadRequestException;
import com.bookstore.common.ResourceNotFoundException;
import com.bookstore.product.Product;
import com.bookstore.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // UC2.3.1 - Xem danh sách yêu thích
    public List<WishlistItem> list(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    // UC2.3.2 - Thêm sản phẩm vào yêu thích
    public WishlistItem add(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("Product already in wishlist");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setProduct(product);
        return wishlistRepository.save(item);
    }

    // UC2.3.3 - Xóa sản phẩm khỏi yêu thích
    public void remove(Long userId, Long productId) {
        WishlistItem item = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));
        wishlistRepository.delete(item);
    }
}
