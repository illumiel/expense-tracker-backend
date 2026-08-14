package com.expensetracker.security;

import com.expensetracker.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Returns the id of the currently authenticated user (set by JwtAuthenticationFilter).
     * Only called from endpoints that are behind authentication, so it should never fail.
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new UnauthorizedException("Authentication required");
        }
        return userId;
    }
}