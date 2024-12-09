package dev.ramil21.lab4back.service;

import dev.ramil21.lab4back.dto.SignupVerificationResponse;
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

    @Autowired
    PasswordUtil passwordUtil;

    @Autowired
    VerificationUtil verificationUtil;

    @Autowired
    TokenUtil tokenUtil;

    @Autowired
    MailUtil mailUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    public void signup(String email, String password) {
        if (userRepository.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User account already exists for provided email-id");

        System.out.println(email);
        System.out.println(password);
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

    public SignupVerificationResponse verification(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid or expired verification token"));
        if (user.getIsVerified()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already verified");
        }
        user.setIsVerified(true);
        user.setVerificationToken(null); // Удалить токен, чтобы он больше не использовался
        userRepository.save(user);

        String accessToken = tokenUtil.generateAccessToken(user.getEmail(), user.getRole().toString(), user.getId());
        String refreshToken = tokenUtil.generateRefreshToken(user.getEmail());

        RefreshToken dbRefreshToken = RefreshToken.builder()
                .tokenHash(refreshToken)
                .user(user)
                .deviceOn("Unknown Device")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(dbRefreshToken);

        return new SignupVerificationResponse(accessToken, refreshToken);
    }
}