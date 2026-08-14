package com.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRequest {

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotNull(message = "monthlyLimit is required")
    @DecimalMin(value = "0.01", message = "monthlyLimit must be greater than 0")
    private BigDecimal monthlyLimit;
}