package com.expensetracker.repository;

import com.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserId(Long userId);

    List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    List<Expense> findByUserIdAndCategoryIdAndDateBetween(Long userId, Long categoryId,
                                                          LocalDate start, LocalDate end);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    long countByCategoryId(Long categoryId);
}