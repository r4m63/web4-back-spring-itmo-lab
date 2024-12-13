package dev.ramil21.lab4back.security;

import dev.ramil21.lab4back.util.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenUtil tokenUtil;

    public JwtAuthenticationFilter(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String email;

        // Пропускаем запросы на публичные маршруты (например, /signup, /login)
        if (request.getRequestURI().equals("/signup") || request.getRequestURI().equals("/signup/verification") ||
                request.getRequestURI().equals("/login") || request.getRequestURI().equals("/refresh-tokens")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Проверяем, есть ли заголовок Authorization и начинается ли он с Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtToken = authHeader.substring(7); // Убираем "Bearer "
        try {
            // Проверяем токен
            if (!tokenUtil.validateToken(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Получаем email из токена
            email = tokenUtil.getEmailFromToken(jwtToken);
        } catch (Exception e) {
            // Если токен недействителен или истек
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Устанавливаем аутентификацию
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, null, null); // Здесь можно добавить роли
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}