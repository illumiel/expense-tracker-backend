package com.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {

    private BigDecimal totalSpentThisMonth;

    // For chart data (e.g. doughnut/pie chart on the frontend)
    private List<CategorySpending> categorySpending;

    // Budget vs actual per category
    private List<BudgetComparison> budgetComparison;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySpending {
        private Long categoryId;
        private String categoryName;
        private String colorHex;
        private String icon;
        private BigDecimal total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetComparison {
        private Long categoryId;
        private String categoryName;
        private BigDecimal monthlyLimit;
        private BigDecimal spent;
        private BigDecimal remaining;
        private int percentageUsed;
    }
}