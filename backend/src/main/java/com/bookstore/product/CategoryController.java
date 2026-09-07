package com.bookstore.product;

import com.bookstore.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping // public list used by product filters (UC2.1.2 nav) and category picker
    public ApiResponse<List<Category>> list() {
        return ApiResponse.ok(categoryRepository.findAll());
    }
}
