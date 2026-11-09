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

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // UC4.1.3 - Preview order
    public OrderPreviewDto preview(Long userId, Long addressId) {

        CartDto cart = cartService.view(userId);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address address = addressService.requireOwned(userId, addressId);

        String summary = address.getRecipientName()
                + " - " + address.getPhone()
                + " - " + address.getLine1()
                + ", " + address.getDistrict()
                + ", " + address.getCity();

        return new OrderPreviewDto(cart, addressId, summary);
    }

    // UC4.1.1 / UC4.1.4 / UC4.2.1-4
    // Create order with ONLINE or OFFLINE payment.
    @Transactional
    public OrderDto checkout(Long userId, CheckoutRequest req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressService.requireOwned(
                userId,
                req.getAddressId());

        Cart cart = cartService.getOrCreateCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        PaymentMethod paymentMethod = req.getPaymentMethod();

        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.OFFLINE;
        }

        Order order = new Order();

        order.setUser(user);
        order.setShippingAddress(address);

        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(PaymentStatus.UNPAID);

        /*
         * ONLINE:
         * Wait for MONA Pay payment confirmation.
         *
         * OFFLINE:
         * Confirm order immediately (Cash on Delivery).
         */
        if (paymentMethod == PaymentMethod.ONLINE) {

            order.setOrderStatus(OrderStatus.PENDING);

        } else {

            order.setOrderStatus(OrderStatus.CONFIRMED);

        }

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cart.getItems()) {

            Product product = ci.getProduct();

            // Validate stock
            if (ci.getQuantity() > product.getStockQuantity()) {

                throw new BadRequestException(
                        "Not enough stock for " + product.getName());
            }

            // Create OrderItem
            OrderItem item = new OrderItem();

            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(ci.getQuantity());
            item.setPriceAtPurchase(product.getPrice());

            order.getItems().add(item);

            // Calculate total
            total = total.add(
                    product.getPrice().multiply(
                            BigDecimal.valueOf(ci.getQuantity())));

            // Deduct stock immediately
            product.setStockQuantity(
                    product.getStockQuantity() - ci.getQuantity());

            productRepository.save(product);
        }

        order.setTotalAmount(total);

        orderRepository.save(order);

        // Clear cart after successful order creation
        cartService.clear(userId);

        return toDto(order);
    }

    // UC5.1.1 - Order history
    public List<OrderDto> myOrders(Long userId) {

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // UC5.1.2 / UC5.1.3 - Order detail
    public OrderDto myOrderDetail(Long userId, Long orderId) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return toDto(order);
    }

    // UC5.1.4 - Cancel order
    @Transactional
    public OrderDto cancel(Long userId, Long orderId) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Cannot cancel an already paid order
        if (order.getPaymentStatus() == PaymentStatus.PAID) {

            throw new BadRequestException(
                    "Paid orders cannot be cancelled directly");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING
                && order.getOrderStatus() != OrderStatus.CONFIRMED) {

            throw new BadRequestException(
                    "Order can no longer be cancelled");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        // Restore stock when the customer explicitly cancels the order.
        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            product.setStockQuantity(
                    product.getStockQuantity() + item.getQuantity());

            productRepository.save(product);
        }

        return toDto(orderRepository.save(order));
    }

    // Convert Order entity to DTO
    OrderDto toDto(Order order) {

        List<OrderItemDto> items = order.getItems()
                .stream()
                .map(item -> new OrderItemDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase()))
                .toList();

        String address = order.getShippingAddress().getRecipientName()
                + " - " + order.getShippingAddress().getLine1()
                + ", " + order.getShippingAddress().getDistrict()
                + ", " + order.getShippingAddress().getCity();

        String recipientEmail = order.getShippingAddress() != null && order.getShippingAddress().getEmail() != null
                ? order.getShippingAddress().getEmail()
                : (order.getUser() != null ? order.getUser().getEmail() : null);

        return new OrderDto(
                order.getId(),
                order.getOrderStatus().name(),
                order.getPaymentMethod().name(),
                order.getPaymentReference(),
                order.getTransactionId(),
                order.getPaymentStatus().name(),
                order.getPaidAt(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getShippingAddress().getRecipientName(),
                recipientEmail,
                address,
                items);
    }

    Order requireEntity(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}