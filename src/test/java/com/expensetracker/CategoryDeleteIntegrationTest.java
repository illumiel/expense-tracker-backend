package com.expensetracker;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryDeleteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String token;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.builder()
                .name("Test User")
                .email("cattest@example.com")
                .password(passwordEncoder.encode("secret123"))
                .build());
        token = jwtService.generateToken(user.getId(), user.getEmail());

        Category category = categoryRepository.save(Category.builder()
                .name("Food")
                .colorHex("#FF5733")
                .icon("🍕")
                .user(user)
                .build());
        categoryId = category.getId();
    }

    @Test
    void deleteCategoryWithExpensesReturns409WithClearMessage() throws Exception {
        expenseRepository.save(Expense.builder()
                .amount(new BigDecimal("25.00"))
                .description("Lunch")
                .date(LocalDate.now())
                .category(categoryRepository.findById(categoryId).orElseThrow())
                .user(userRepository.findAll().stream().findFirst().orElseThrow())
                .build());

        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value(containsString("Cannot delete category 'Food' because it has 1 associated expense(s)")));
    }

    @Test
    void deleteCategoryWithoutExpensesSucceeds() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategoryWithoutTokenReturns401() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isUnauthorized());
    }
}