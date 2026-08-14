package com.expensetracker.controller;

import com.expensetracker.dto.BudgetRequest;
import com.expensetracker.dto.BudgetResponse;
import com.expensetracker.security.SecurityUtils;
import com.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public List<BudgetResponse> getBudgets() {
        return budgetService.getBudgetsByUser(SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/{id}")
    public BudgetResponse getBudgetById(@PathVariable Long id) {
        return budgetService.getBudgetById(id, SecurityUtils.getCurrentUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse createBudget(@Valid @RequestBody BudgetRequest request) {
        return budgetService.createBudget(SecurityUtils.getCurrentUserId(), request);
    }

    @PutMapping("/{id}")
    public BudgetResponse updateBudget(@PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        return budgetService.updateBudget(id, SecurityUtils.getCurrentUserId(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id, SecurityUtils.getCurrentUserId());
    }
}