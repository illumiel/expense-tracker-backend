package com.expensetracker.service;

import com.expensetracker.dto.CategoryRequest;
import com.expensetracker.dto.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.exception.CategoryInUseException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByUser(Long userId) {
        return categoryRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id, Long userId) {
        return toResponse(findCategory(id, userId));
    }

    @Transactional
    public CategoryResponse createCategory(Long userId, CategoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Category category = Category.builder()
                .name(request.getName())
                .colorHex(request.getColorHex())
                .icon(request.getIcon())
                .user(user)
                .build();
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, Long userId, CategoryRequest request) {
        Category category = findCategory(id, userId);
        category.setName(request.getName());
        category.setColorHex(request.getColorHex());
        category.setIcon(request.getIcon());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id, Long userId) {
        Category category = findCategory(id, userId);
        long expenseCount = expenseRepository.countByCategoryId(category.getId());
        if (expenseCount > 0) {
            throw new CategoryInUseException(category.getName(), expenseCount);
        }
        categoryRepository.delete(category);
    }

    private Category findCategory(Long id, Long userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .colorHex(category.getColorHex())
                .icon(category.getIcon())
                .userId(category.getUser().getId())
                .build();
    }
}