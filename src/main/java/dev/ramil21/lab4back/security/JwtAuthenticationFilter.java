package dev.ramil21.lab4back.security;

import dev.ramil21.lab4back.util.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenUtil tokenUtil;

    @Autowired
    public JwtAuthenticationFilter(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    private static final Set<String> SKIP_PATHS = Set.of(
            "/auth/signup",
            "/auth/login",
            "/auth/restorePassword"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        // Пропускаем запросы для путей, не требующих авторизации
        if (SKIP_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = authHeader.substring(7); // Убираем "Bearer "
        try {
            if (!tokenUtil.validateToken(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String email = tokenUtil.getEmailFromToken(jwtToken);

            // Помещаем email напрямую в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(new SimpleAuthentication(email));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Вспомогательный класс для хранения email
    private static class SimpleAuthentication implements Authentication {
        private final String email;

        public SimpleAuthentication(String email) {
            this.email = email;
        }

        @Override
        public String getPrincipal() {
            return email; // principal — это email
        }

        // Пустые реализации остальных методов Authentication
        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public String getName() {
            return email;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        }
    }

}