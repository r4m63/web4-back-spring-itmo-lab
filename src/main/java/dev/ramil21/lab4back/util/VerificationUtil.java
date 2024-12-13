package dev.ramil21.lab4back.util;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.util.Base64;


@Component
public class VerificationUtil {
    private static final int TOKEN_LENGTH = 64; // Длина токена в байтах
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateVerificationToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Создает URL для верификации на основе токена.
     * Output: http://localhost:8080/signup/verification?token=...
     *
     * @param baseUrl базовый URL приложения (например, http://localhost:8080)
     * @param token   токен для верификации
     * @return URL с токеном
     */
    public String createVerificationUrlByToken(String baseUrl, String token) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/signup/verification")
                .queryParam("token", token)
                .toUriString();
    }
}
