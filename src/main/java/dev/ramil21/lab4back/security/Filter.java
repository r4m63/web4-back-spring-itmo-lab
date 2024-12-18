package dev.ramil21.lab4back.security;

import dev.ramil21.lab4back.util.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class Filter extends OncePerRequestFilter {

    final TokenUtil tokenUtil;

    @Autowired
    public Filter(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Извлекаем JWT токен из cookie
        String token = null;
        Cookie[] cookies = request.getCookies();
        System.out.println("COOKIES: " + Arrays.toString(cookies));
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    System.out.println("token: " + token);
                    break;
                }
            }
        }

        // Если токен найден, извлекаем email из него
        if (token != null) {
            try {
                String email = tokenUtil.getEmailFromToken(token);
                System.out.println(email);
                if (email != null) {
                    SecurityContextHolder.getContext().setAuthentication(new SimpleAuth(email));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        filterChain.doFilter(request, response);
    }
}


