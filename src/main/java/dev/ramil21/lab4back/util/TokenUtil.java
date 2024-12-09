package dev.ramil21.lab4back.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenUtil {
    private static final String SECRET_KEY = "YourSuperSecretKeyForJWT_ChangeThisKey12345";
    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 15; // 15 минут
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7; // 7 дней

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateAccessToken(String email, String role, long id) {
        return generateToken(email, role, id, ACCESS_TOKEN_VALIDITY);
    }

    // Генерация Refresh Token
    public String generateRefreshToken(String email) {
        return generateToken(email, null, null, REFRESH_TOKEN_VALIDITY);
    }


    // Общий метод генерации токенов
    private String generateToken(String email, String role, Long id, long validity) {
        Map<String, Object> claims = new HashMap<>();
        if (role != null) {
            claims.put("roles", role); // Добавляем роли в токен
        }
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Валидация токена
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Логика обработки ошибок токена
            return false;
        }
    }

    // Извлечение username из токена
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Извлечение ролей из токена
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("roles", String.class);
    }
}

// Интеграция с Spring Security
// Создайте фильтр для проверки токенов и интеграции с контекстом Spring Security.
// Добавьте фильтр в цепочку фильтров Spring Security.

//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final TokenUtil tokenUtil;
//
//    public JwtAuthenticationFilter(TokenUtil tokenUtil) {
//        this.tokenUtil = tokenUtil;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        String header = request.getHeader("Authorization");
//        if (header != null && header.startsWith("Bearer ")) {
//            String token = header.substring(7);
//            if (tokenUtil.validateToken(token)) {
//                String username = tokenUtil.getUsernameFromToken(token);
//
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(username, null, null);
//                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}

//@Override
//protected void configure(HttpSecurity http) throws Exception {
//    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//}