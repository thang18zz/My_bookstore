package com.bookstore.order;

import com.bookstore.address.Address;
import com.bookstore.address.AddressService;
import com.bookstore.auth.User;
import com.bookstore.auth.UserRepository;
import com.bookstore.cart.Cart;
import com.bookstore.cart.CartItem;
import com.bookstore.cart.CartService;
import com.bookstore.cart.dto.CartDto;
import com.bookstore.common.BadRequestException;
import com.bookstore.common.ResourceNotFoundException;
import com.bookstore.order.dto.*;
import com.bookstore.product.Product;
import com.bookstore.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** This is where Theme 3 (cart), Theme 6 (address) and Theme 4/5 (order) meet: checkout reads the
 *  live Cart and a saved Address, then produces an immutable Order + snapshot OrderItems and adjusts
 *  Product stock - implementing UC4.1.1-4 and UC4.2.1-4 in one transaction. */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // UC4.1.3 - Kiểm tra thông tin đơn hàng trước khi xác nhận
    public OrderPreviewDto preview(Long userId, Long addressId) {
        CartDto cart = cartService.view(userId);
        if (cart.getItems().isEmpty()) throw new BadRequestException("Cart is empty");
        Address address = addressService.requireOwned(userId, addressId); // UC4.1.2
        String summary = address.getRecipientName() + " - " + address.getPhone() + " - " + address.getLine1()
                + ", " + address.getDistrict() + ", " + address.getCity();
        return new OrderPreviewDto(cart, addressId, summary);
    }

    // UC4.1.1 / UC4.1.4 / UC4.2.1-4 - Tiến hành thanh toán -> xác nhận đặt hàng -> tạo đơn hàng
    @Transactional
    public OrderDto checkout(Long userId, CheckoutRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Address address = addressService.requireOwned(userId, req.getAddressId());
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) throw new BadRequestException("Cart is empty");

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            Product product = ci.getProduct();
            // Re-validate stock at the moment of purchase (UC3.2.1/UC3.2.2 checked again here)
            if (ci.getQuantity() > product.getStockQuantity()) {
                throw new BadRequestException("Not enough stock for " + product.getName());
            }
            OrderItem item = new OrderItem(); // UC4.2.3 - Ghi nhận sản phẩm trong đơn hàng
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(ci.getQuantity());
            item.setPriceAtPurchase(product.getPrice());
            order.getItems().add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));

            product.setStockQuantity(product.getStockQuantity() - ci.getQuantity()); // UC4.2.4
            productRepository.save(product);
        }
        order.setTotalAmount(total); // UC4.2.2
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        cartService.clear(userId); // cart consumed once the order exists

        return toDto(order);
    }

    // UC5.1.1 - Xem lịch sử đơn hàng
    public List<OrderDto> myOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).toList();
    }

    // UC5.1.2 - Xem chi tiết đơn hàng (customer, ownership enforced) / UC5.1.3 tracking is the same view
    public OrderDto myOrderDetail(Long userId, Long orderId) {
        Order o = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toDto(o);
    }

    // UC5.1.4 - Hủy đơn hàng (chỉ khi còn ở trạng thái cho phép hủy)
    @Transactional
    public OrderDto cancel(Long userId, Long orderId) {
        Order o = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (o.getStatus() != OrderStatus.PENDING && o.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order can no longer be cancelled");
        }
        o.setStatus(OrderStatus.CANCELLED);
        // restock
        for (OrderItem item : o.getItems()) {
            Product p = item.getProduct();
            p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
            productRepository.save(p);
        }
        return toDto(orderRepository.save(o));
    }

    OrderDto toDto(Order o) {
        List<OrderItemDto> items = o.getItems().stream()
                .map(i -> new OrderItemDto(i.getProduct().getId(), i.getProduct().getName(),
                        i.getQuantity(), i.getPriceAtPurchase()))
                .toList();
        String addr = o.getShippingAddress().getRecipientName() + " - " + o.getShippingAddress().getLine1()
                + ", " + o.getShippingAddress().getDistrict() + ", " + o.getShippingAddress().getCity();
        return new OrderDto(o.getId(), o.getStatus().name(), o.getTotalAmount(), o.getCreatedAt(),
                o.getShippingAddress().getRecipientName(), addr, items);
    }

    Order requireEntity(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}
