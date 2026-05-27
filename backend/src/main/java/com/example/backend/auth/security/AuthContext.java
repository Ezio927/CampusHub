package com.example.backend.auth.security;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserRole;

public final class AuthContext {

    private static final ThreadLocal<AuthUser> CURRENT = new ThreadLocal<AuthUser>();

    private AuthContext() {
    }

    public static void setCurrentUser(AuthUser user) {
        CURRENT.set(user);
    }

    public static AuthUser getCurrentUser() {
        return CURRENT.get();
    }

    public static AuthUser requireCurrentUser() {
        AuthUser user = CURRENT.get();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    public static Long requireUserId() {
        return requireCurrentUser().getUserId();
    }

    public static boolean hasRole(UserRole role) {
        AuthUser user = CURRENT.get();
        return user != null && user.getRole() == role;
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static final class AuthUser {
        private final Long userId;
        private final String username;
        private final UserRole role;

        public AuthUser(Long userId, String username, UserRole role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public UserRole getRole() {
            return role;
        }
    }
}
