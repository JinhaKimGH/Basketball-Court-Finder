package com.basketballcourtfinder.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    public static Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        return (Long) authentication.getPrincipal(); // Assuming user ID is stored as principal
    }

    // Static method to check if the request is from localhost or 127.0.0.1
    public static boolean isLocal(HttpServletRequest request) {
        String host = request.getHeader("Host");
        return host != null && (host.contains("localhost") || host.contains("127.0.0.1"));
    }
}
