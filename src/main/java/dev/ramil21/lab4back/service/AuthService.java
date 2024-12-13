package dev.ramil21.lab4back.service;

import dev.ramil21.lab4back.dto.TokensResponse;
import dev.ramil21.lab4back.model.RefreshToken;
import dev.ramil21.lab4back.model.Role;
import dev.ramil21.lab4back.model.User;
import dev.ramil21.lab4back.repository.RefreshTokenRepository;
import dev.ramil21.lab4back.repository.UserRepository;
import dev.ramil21.lab4back.util.MailUtil;
import dev.ramil21.lab4back.util.PasswordUtil;
import dev.ramil21.lab4back.util.TokenUtil;
import dev.ramil21.lab4back.util.VerificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    PasswordUtil passwordUtil;
    VerificationUtil verificationUtil;
    TokenUtil tokenUtil;
    MailUtil mailUtil;

    UserRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public AuthService(PasswordUtil passwordUtil, VerificationUtil verificationUtil, TokenUtil tokenUtil,
                       MailUtil mailUtil, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.passwordUtil = passwordUtil;
        this.verificationUtil = verificationUtil;
        this.tokenUtil = tokenUtil;
        this.mailUtil = mailUtil;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void signup(String email, String password) {
        if (userRepository.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User account already exists for provided email-id");

        String verificationToken = verificationUtil.generateVerificationToken();
        // TODO: вынести в ENUM http://localhost:8080 - как BASE_URL и все ENUMы всех путей тоже
        String verificationUrl = verificationUtil.createVerificationUrlByToken("http://localhost:8080", verificationToken);
        var user = User.builder()
                .email(email)
                .passwordHash(passwordUtil.hashPassword(password))
                .isVerified(false)
                .verificationToken(verificationToken)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        var savedUser = userRepository.save(user);

        mailUtil.send(savedUser.getEmail(), "Subject", "Перейдите по ссылке чтобы подтвердить аккаунт" + verificationUrl);
    }

    public TokensResponse verification(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid or expired verification token"));
        if (user.getIsVerified()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already verified");
        }
        user.setIsVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        String accessToken = tokenUtil.generateAccessToken(user.getEmail(), user.getRole().toString(), user.getId());
        String refreshToken = tokenUtil.generateRefreshToken(user.getEmail());
        RefreshToken dbRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(dbRefreshToken);

        return new TokensResponse(accessToken, refreshToken);
    }

    public TokensResponse login(String email, String password) {
        final User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login credentials")); //TODO: fix http status + description

        if (!passwordUtil.verifyPassword(password, user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login credentials"); //TODO: fix http status + description

        String accessToken = tokenUtil.generateAccessToken(user.getEmail(), user.getRole().toString(), user.getId());
        String refreshToken = tokenUtil.generateRefreshToken(user.getEmail());
        RefreshToken dbRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(dbRefreshToken);

        return new TokensResponse(accessToken, refreshToken);
    }

    public TokensResponse refreshTokens(String inputUserRefreshToken) {
        if (tokenUtil.isTokenExpired(inputUserRefreshToken))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");

        final var user = userRepository.findByEmail(tokenUtil.getEmailFromToken(inputUserRefreshToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        String accessToken = tokenUtil.generateAccessToken(user.getEmail(), user.getRole().toString(), user.getId());
        String refreshToken = tokenUtil.generateRefreshToken(user.getEmail());
        RefreshToken dbRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(dbRefreshToken);

        return new TokensResponse(accessToken, refreshToken);
    }
}