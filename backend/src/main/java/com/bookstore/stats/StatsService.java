package com.bookstore.stats;

import com.bookstore.auth.Role;
import com.bookstore.auth.UserRepository;
import com.bookstore.order.Order;
import com.bookstore.order.OrderItem;
import com.bookstore.order.OrderRepository;
import com.bookstore.order.OrderStatus;
import com.bookstore.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Theme 12 - Thống kê & báo cáo. Reads directly from order/product/customer tables rather than a
 *  separate reporting store, so numbers here can never drift from Theme 4/5/8/10. */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // UC12.1.1-5 - Dashboard tổng quan
    public DashboardDto dashboard() {
        List<Order> delivered = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED).toList();
        BigDecimal revenue = delivered.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardDto(
                productRepository.count(),
                userRepository.findAll().stream().filter(u -> u.getRole() == Role.CUSTOMER).count(),
                orderRepository.count(),
                revenue);
    }

    // UC12.2.1 - Thống kê doanh thu theo khoảng thời gian
    public BigDecimal revenueBetween(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .filter(o -> !o.getCreatedAt().isBefore(from) && !o.getCreatedAt().isAfter(to))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // UC12.2.2 / UC12.2.4 / UC12.2.5 - Sản phẩm bán chạy / số lượng bán / bán chạy nhất
    public List<BestSellerDto> bestSellers(int limit) {
        Map<Long, List<OrderItem>> byProduct = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(i -> i.getProduct().getId()));

        return byProduct.entrySet().stream()
                .map(e -> {
                    long units = e.getValue().stream().mapToLong(OrderItem::getQuantity).sum();
                    String name = e.getValue().get(0).getProduct().getName();
                    return new BestSellerDto(e.getKey(), name, units);
                })
                .sorted(Comparator.comparingLong(BestSellerDto::getUnitsSold).reversed())
                .limit(limit)
                .toList();
    }

    // UC12.2.3 - Thống kê theo danh mục
    public Map<String, BigDecimal> revenueByCategory() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .filter(i -> i.getProduct().getCategory() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getProduct().getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO,
                                i -> i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())),
                                BigDecimal::add)));
    }
}
