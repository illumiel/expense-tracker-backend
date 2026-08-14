package com.expensetracker.service;

import com.expensetracker.dto.BudgetRequest;
import com.expensetracker.dto.BudgetResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.exception.DuplicateBudgetException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByUser(Long userId) {
        return budgetRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id, Long userId) {
        return toResponse(findBudget(id, userId));
    }

    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest request) {
        budgetRepository.findByUserIdAndCategoryId(userId, request.getCategoryId())
                .ifPresent(budget -> {
                    throw new DuplicateBudgetException("A budget already exists for this category");
                });
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        Budget budget = Budget.builder()
                .category(category)
                .monthlyLimit(request.getMonthlyLimit())
                .user(user)
                .build();
        return toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetResponse updateBudget(Long id, Long userId, BudgetRequest request) {
        Budget budget = findBudget(id, userId);
        budget.setMonthlyLimit(request.getMonthlyLimit());
        return toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public void deleteBudget(Long id, Long userId) {
        budgetRepository.delete(findBudget(id, userId));
    }

    private Budget findBudget(Long id, Long userId) {
        return budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
    }

    private BudgetResponse toResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .monthlyLimit(budget.getMonthlyLimit())
                .userId(budget.getUser().getId())
                .build();
    }
}