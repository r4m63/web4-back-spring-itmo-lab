package dev.ramil21.lab4back.util;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.Key;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenUtil {
    private static final String GOOGLE_PUBLIC_KEYS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String SECRET_KEY = "YourSuperSecretKeyForJWT_ChangeThisKey12345";
    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 15; // 15 минут
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7; // 7 дней

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateAccessToken(String email, String role, long id) {
        return generateToken(email, role, id, ACCESS_TOKEN_VALIDITY);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, null, null, REFRESH_TOKEN_VALIDITY);
    }

    private String generateToken(String email, String role, Long id, long validity) {
        Map<String, Object> claims = new HashMap<>();
        if (role != null) {
            claims.put("roles", role); // Добавляем роль в токен
        }
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Логирование ошибки и возврат false
            // Можно использовать логирование для отслеживания проблем с токеном
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("roles", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expirationDate.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // Логирование ошибки и возвращаем true, если токен не валиден
            return true;
        }
    }

    public String getEmailFromGoogleToken(String googleToken) throws Exception {
        try {
            // Декодируем JWT токен, чтобы получить kid (key ID)
            SignedJWT signedJWT = SignedJWT.parse(googleToken);
            String kid = signedJWT.getHeader().getKeyID();

            // Получаем публичные ключи Google для верификации подписи
            JWKSet jwkSet = JWKSet.load(new URL(GOOGLE_PUBLIC_KEYS_URL));

            // Ищем ключ, соответствующий kid
            JWK jwk = jwkSet.getKeyByKeyId(kid);

            // Проверяем, что найденный ключ является RSA публичным ключом
            if (jwk instanceof RSAKey) {
                RSAKey rsaKey = (RSAKey) jwk;

                // Получаем публичный ключ
                RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

                // Верифицируем подпись токена
                RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
                if (!signedJWT.verify(verifier)) {
                    throw new RuntimeException("Invalid JWT signature");
                }

                // Извлекаем email из токена
                return signedJWT.getJWTClaimsSet().getStringClaim("email");
            } else {
                throw new RuntimeException("Public key is not RSA key");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error verifying JWT token", e);
        }
    }
}
