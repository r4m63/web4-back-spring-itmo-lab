package dev.ramil21.lab4back.service;


import dev.ramil21.lab4back.dto.auth.AuthResponse;
import dev.ramil21.lab4back.dto.auth.RegisterRequest;
import dev.ramil21.lab4back.model.User;
import dev.ramil21.lab4back.repository.token.RefreshTokenRepository;
import dev.ramil21.lab4back.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public AuthResponse register(RegisterRequest request) {
        //
        //
        var user = User.builder()
                .email(request.getEmail())
                .passwordHash()
                .salt()
                .verificationToken()
                .build();
        User savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
}
