package dev.ramil21.lab4back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // Отключаем CSRF
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()  // Разрешаем все запросы (можно настроить по своему)
                )
                .formLogin(AbstractHttpConfigurer::disable)       // Отключаем форму логина
                .httpBasic(AbstractHttpConfigurer::disable)       // Отключаем HTTP Basic аутентификацию
                .logout(AbstractHttpConfigurer::disable)          // Отключаем выход
                .sessionManagement(AbstractHttpConfigurer::disable) // Отключаем управление сессиями
                .exceptionHandling(AbstractHttpConfigurer::disable) // Отключаем обработку исключений
                .securityContext(AbstractHttpConfigurer::disable)  // Отключаем SecurityContext
                .anonymous(AbstractHttpConfigurer::disable)        // Отключаем AnonymousAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Добавляем ваш фильтр

        return http.build();
    }
}
