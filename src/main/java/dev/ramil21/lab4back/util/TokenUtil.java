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
    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7; // 15 минут
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
        if (email != null) {
            claims.put("email", email);
        }
        if (role != null) {
            claims.put("role", role);
        }
        if (id != null) {
            claims.put("id", id);
        }
        claims.put("validity", validity);
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
            return false;
        }
    }

    public Long getIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("id", Long.class);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public String getEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("email", String.class);
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
            return claims.get("role", String.class);
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
            return true;
        }
    }

    public String getEmailFromGoogleToken(String googleToken) throws Exception {
        try {
            SignedJWT signedJWT = SignedJWT.parse(googleToken);
            String kid = signedJWT.getHeader().getKeyID();
            JWKSet jwkSet = JWKSet.load(new URL(GOOGLE_PUBLIC_KEYS_URL));
            JWK jwk = jwkSet.getKeyByKeyId(kid);

            if (jwk instanceof RSAKey) {
                RSAKey rsaKey = (RSAKey) jwk;
                RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
                RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
                if (!signedJWT.verify(verifier)) {
                    throw new RuntimeException("Invalid JWT signature");
                }
                return signedJWT.getJWTClaimsSet().getStringClaim("email");
            } else {
                throw new RuntimeException("Public key is not RSA key");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error verifying JWT token", e);
        }
    }
}
