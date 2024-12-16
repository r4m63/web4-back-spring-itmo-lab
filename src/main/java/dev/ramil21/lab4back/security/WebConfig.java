package dev.ramil21.lab4back.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173/")  // Разрешить запросы с фронтенда
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Разрешить методы
                .allowedHeaders("*")  // Разрешить все заголовки
                .allowCredentials(true); // Разрешить куки
    }
}
