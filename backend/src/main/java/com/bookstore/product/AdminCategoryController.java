package com.bookstore.product;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.BadRequestException;
import com.bookstore.common.ResourceNotFoundException;
import com.bookstore.product.dto.CategoryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Theme 9 - Quản lý danh mục (Admin): UC9.1.1-5. */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping // UC9.1.1
    public ApiResponse<List<Category>> list() {
        return ApiResponse.ok(categoryRepository.findAll());
    }

    @GetMapping("/{id}") // UC9.1.2
    public ApiResponse<Category> detail(@PathVariable Long id) {
        return ApiResponse.ok(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
    }

    @PostMapping // UC9.1.3
    public ApiResponse<Category> create(@Valid @RequestBody CategoryRequest req) {
        if (categoryRepository.existsByNameIgnoreCase(req.getName())) {
            throw new BadRequestException("Category already exists");
        }
        Category c = new Category();
        c.setName(req.getName());
        c.setDescription(req.getDescription());
        return ApiResponse.ok("Category created", categoryRepository.save(c));
    }

    @PutMapping("/{id}") // UC9.1.4
    public ApiResponse<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        c.setName(req.getName());
        c.setDescription(req.getDescription());
        return ApiResponse.ok("Category updated", categoryRepository.save(c));
    }

    @DeleteMapping("/{id}") // UC9.1.5
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ApiResponse.ok("Category deleted", null);
    }
}
