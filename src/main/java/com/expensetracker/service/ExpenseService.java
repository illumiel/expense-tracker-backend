package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.dto.SummaryResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(Long userId, Long categoryId, LocalDate from, LocalDate to) {
        boolean hasDateRange = from != null || to != null;
        LocalDate start = from != null ? from : LocalDate.MIN;
        LocalDate end = to != null ? to : LocalDate.now().plusYears(1);

        List<Expense> expenses;
        if (categoryId != null && hasDateRange) {
            expenses = expenseRepository.findByUserIdAndCategoryIdAndDateBetween(userId, categoryId, start, end);
        } else if (categoryId != null) {
            expenses = expenseRepository.findByUserIdAndCategoryId(userId, categoryId);
        } else if (hasDateRange) {
            expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);
        } else {
            expenses = expenseRepository.findByUserId(userId);
        }
        return expenses.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id, Long userId) {
        return toResponse(findExpense(id, userId));
    }

    @Transactional
    public ExpenseResponse createExpense(Long userId, ExpenseRequest request) {
        User user = getUser(userId);
        Category category = getCategory(request.getCategoryId(), userId);
        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .category(category)
                .user(user)
                .build();
        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, Long userId, ExpenseRequest request) {
        Expense expense = findExpense(id, userId);
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setCategory(getCategory(request.getCategoryId(), userId));
        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void deleteExpense(Long id, Long userId) {
        expenseRepository.delete(findExpense(id, userId));
    }

    @Transactional(readOnly = true)
    public SummaryResponse getSummary(Long userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<Expense> monthExpenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

        // Total spent this month
        BigDecimal totalSpent = monthExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Spending grouped by category (chart data)
        Map<Long, SummaryResponse.CategorySpending> byCategory = new LinkedHashMap<>();
        for (Expense expense : monthExpenses) {
            Category category = expense.getCategory();
            SummaryResponse.CategorySpending entry = byCategory.computeIfAbsent(
                    category.getId(),
                    c -> SummaryResponse.CategorySpending.builder()
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .colorHex(category.getColorHex())
                            .icon(category.getIcon())
                            .total(BigDecimal.ZERO)
                            .build());
            entry.setTotal(entry.getTotal().add(expense.getAmount()));
        }

        // Budget vs actual
        List<SummaryResponse.BudgetComparison> comparisons = new ArrayList<>();
        for (Budget budget : budgetRepository.findByUserId(userId)) {
            BigDecimal spent = monthExpenses.stream()
                    .filter(e -> e.getCategory().getId().equals(budget.getCategory().getId()))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal limit = budget.getMonthlyLimit();
            int percentageUsed = limit.compareTo(BigDecimal.ZERO) > 0
                    ? spent.multiply(BigDecimal.valueOf(100)).divide(limit, 0, RoundingMode.HALF_UP).intValue()
                    : 0;
            comparisons.add(SummaryResponse.BudgetComparison.builder()
                    .categoryId(budget.getCategory().getId())
                    .categoryName(budget.getCategory().getName())
                    .monthlyLimit(limit)
                    .spent(spent)
                    .remaining(limit.subtract(spent))
                    .percentageUsed(percentageUsed)
                    .build());
        }

        return SummaryResponse.builder()
                .totalSpentThisMonth(totalSpent)
                .categorySpending(new ArrayList<>(byCategory.values()))
                .budgetComparison(comparisons)
                .build();
    }

    private Expense findExpense(Long id, Long userId) {
        return expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Category getCategory(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

    private ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .date(expense.getDate())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .categoryColorHex(expense.getCategory().getColorHex())
                .userId(expense.getUser().getId())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}