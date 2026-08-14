package com.expensetracker.controller;

import com.expensetracker.dto.UserRequest;
import com.expensetracker.dto.UserResponse;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.security.SecurityUtils;
import com.expensetracker.service.UserService;
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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Users only ever see/operate on their own account
    @GetMapping
    public UserResponse getCurrentUser() {
        return userService.getUserById(SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        requireSelf(id);
        return userService.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        requireSelf(id);
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        requireSelf(id);
        userService.deleteUser(id);
    }

    // Treat other users' ids as if they don't exist (avoids leaking ids via 403s)
    private void requireSelf(Long id) {
        if (!id.equals(SecurityUtils.getCurrentUserId())) {
            throw new ResourceNotFoundException("User", "id", id);
        }
    }
}