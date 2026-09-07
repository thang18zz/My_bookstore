package com.bookstore.cart;

import com.bookstore.auth.User;
import com.bookstore.auth.UserRepository;
import com.bookstore.cart.dto.*;
import com.bookstore.common.BadRequestException;
import com.bookstore.common.ResourceNotFoundException;
import com.bookstore.product.Product;
import com.bookstore.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/** Implements Epic 3.1 (manage cart) and Epic 3.2 (stock checks). order.OrderService reads this cart
 *  directly when a customer checks out (UC4.2.1), which is how Theme 3 and Theme 4 are linked. */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // UC3.1.1 + UC3.1.6 - Xem giỏ hàng + tính tổng tiền
    public CartDto view(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItemDto> items = cart.getItems().stream().map(this::toItemDto).toList();
        BigDecimal total = items.stream().map(CartItemDto::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartDto(items, total);
    }

    // UC3.1.2 - Thêm sản phẩm vào giỏ hàng (includes the UC3.2.1/UC3.2.2 stock check up front)
    public CartDto addItem(Long userId, AddCartItemRequest req) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId())).findFirst().orElse(null);
        int newQty = (existing != null ? existing.getQuantity() : 0) + req.getQuantity();
        checkStock(product, newQty); // UC3.2.1 / UC3.2.2

        if (existing != null) {
            existing.setQuantity(newQty);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(req.getQuantity());
            cart.getItems().add(item);
        }
        cartRepository.save(cart);
        return view(userId);
    }

    // UC3.1.3 - Cập nhật số lượng sản phẩm
    public CartDto updateItem(Long userId, Long productId, UpdateCartItemRequest req) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not in cart"));
        checkStock(item.getProduct(), req.getQuantity());
        item.setQuantity(req.getQuantity());
        cartRepository.save(cart);
        return view(userId);
    }

    // UC3.1.4 - Xóa sản phẩm khỏi giỏ hàng
    public CartDto removeItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        cartRepository.save(cart);
        return view(userId);
    }

    // UC3.1.5 - Xóa toàn bộ giỏ hàng
    public void clear(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // UC3.2.1 / UC3.2.2 - Kiểm tra tồn kho / số lượng có thể mua (reused by checkout too, see OrderService)
    public void checkStock(Product product, int requestedQty) {
        if (product.getStockQuantity() <= 0) {
            throw new BadRequestException(product.getName() + " is out of stock");
        }
        if (requestedQty > product.getStockQuantity()) {
            throw new BadRequestException("Only " + product.getStockQuantity() + " left for " + product.getName());
        }
    }

    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            Cart c = new Cart();
            c.setUser(user);
            return cartRepository.save(c);
        });
    }

    private CartItemDto toItemDto(CartItem i) {
        BigDecimal subtotal = i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
        return new CartItemDto(i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getImageUrl(),
                i.getProduct().getPrice(), i.getQuantity(), i.getProduct().getStockQuantity(), subtotal);
    }
}
