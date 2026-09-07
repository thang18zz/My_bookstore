package com.bookstore.stats;

import com.bookstore.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class DashboardController {

    private final StatsService statsService;

    @GetMapping("/dashboard") // UC12.1.1-5
    public ApiResponse<DashboardDto> dashboard() {
        return ApiResponse.ok(statsService.dashboard());
    }

    @GetMapping("/revenue") // UC12.2.1
    public ApiResponse<BigDecimal> revenue(@RequestParam LocalDateTime from, @RequestParam LocalDateTime to) {
        return ApiResponse.ok(statsService.revenueBetween(from, to));
    }

    @GetMapping("/best-sellers") // UC12.2.2 / UC12.2.4 / UC12.2.5
    public ApiResponse<List<BestSellerDto>> bestSellers(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(statsService.bestSellers(limit));
    }

    @GetMapping("/by-category") // UC12.2.3
    public ApiResponse<Map<String, BigDecimal>> byCategory() {
        return ApiResponse.ok(statsService.revenueByCategory());
    }
}
