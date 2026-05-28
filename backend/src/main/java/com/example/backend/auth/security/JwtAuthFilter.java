package com.example.backend.auth.security;

import com.example.backend.common.ApiResponse;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserRole;
import com.example.backend.config.JwtConfig;
import com.example.backend.common.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register")
                || path.startsWith("/error")
                || path.startsWith("/api-docs")
                || path.startsWith("/docs")) {
            return true;
        }

        // Public GET: user profile, demand search, demand detail
        if ("GET".equalsIgnoreCase(method)) {
            if (path.equals("/api/users") || (path.startsWith("/api/users/") && !path.startsWith("/api/users/me"))) return true;
            if (path.equals("/api/demands") || path.startsWith("/api/demands/")) return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token == null || token.isEmpty()) {
                writeUnauthorized(response, ErrorCode.UNAUTHORIZED);
                return;
            }
            if (!jwtTokenProvider.validate(token)) {
                writeUnauthorized(response, ErrorCode.TOKEN_INVALID);
                return;
            }
            AuthContext.setCurrentUser(new AuthContext.AuthUser(
                    jwtTokenProvider.parseUserId(token),
                    jwtTokenProvider.parseUsername(token),
                    jwtTokenProvider.parseRole(token)
            ));
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.trim().isEmpty()) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7).trim();
        }
        return authorization.trim();
    }

    private void writeUnauthorized(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":" + errorCode.getCode() + ",\"message\":\"" + errorCode.getMessage() + "\",\"data\":null}";
        response.getWriter().write(body);
    }
}
