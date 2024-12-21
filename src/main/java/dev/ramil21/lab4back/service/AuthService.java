package dev.ramil21.lab4back.service;

import dev.ramil21.lab4back.dto.AccessToken;
import dev.ramil21.lab4back.model.PassResetToken;
import dev.ramil21.lab4back.model.RefreshToken;
import dev.ramil21.lab4back.model.Role;
import dev.ramil21.lab4back.model.User;
import dev.ramil21.lab4back.repository.PassResetTokenRepository;
import dev.ramil21.lab4back.repository.RefreshTokenRepository;
import dev.ramil21.lab4back.repository.UserRepository;
import dev.ramil21.lab4back.security.ApiPath;
import dev.ramil21.lab4back.util.PasswordUtil;
import dev.ramil21.lab4back.util.TokenUtil;
import dev.ramil21.lab4back.util.UrlUtil;
import dev.ramil21.lab4back.util.mail.MailTemplates;
import dev.ramil21.lab4back.util.mail.MailUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    PasswordUtil passwordUtil;
    UrlUtil urlUtil;
    TokenUtil tokenUtil;
    MailUtil mailUtil;

    UserRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;
    PassResetTokenRepository passResetTokenRepository;

    @Autowired
    public AuthService(PasswordUtil passwordUtil, UrlUtil urlUtil, TokenUtil tokenUtil,
                       MailUtil mailUtil, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PassResetTokenRepository passResetTokenRepository) {
        this.passwordUtil = passwordUtil;
        this.urlUtil = urlUtil;
        this.tokenUtil = tokenUtil;
        this.mailUtil = mailUtil;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passResetTokenRepository = passResetTokenRepository;
    }

    public void signup(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        String verificationToken = urlUtil.generateRandomToken();
        String verificationUrl = urlUtil.createVerificationUrlByToken(ApiPath.FRONTEND_BASE_URL_DEV.get(), verificationToken);

        if (userOpt.isPresent()) {
            // Если пользователь уже существует, но зарегистрирован через Google

            User user = userOpt.get();
            if (user.getIsGoogleAuth()) {
                // Обновляем существующего пользователя
                user.setPasswordHash(passwordUtil.hashPassword(password));
                user.setIsVerified(false);
                user.setVerificationToken(verificationToken);
                user.setUpdatedAt(LocalDateTime.now());
                var savedUser = userRepository.save(user);
                mailUtil.sendHtmlMessage(
                        savedUser.getEmail(),
                        MailTemplates.SUBJECT_REGISTRATION.get(),
                        MailTemplates.BODY_REGISTRATION.set(verificationUrl)
                );
                return;
            } else {
                // Если это обычный пользователь, выбрасываем ошибку
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User account already exists for provided email-id");
            }
        }

        var user = User.builder()
                .email(email)
                .passwordHash(passwordUtil.hashPassword(password))
                .isVerified(false)
                .verificationToken(verificationToken)
                .role(Role.USER)
                .isGoogleAuth(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        var savedUser = userRepository.save(user);

        mailUtil.sendHtmlMessage(
                savedUser.getEmail(),
                MailTemplates.SUBJECT_REGISTRATION.get(),
                MailTemplates.BODY_REGISTRATION.set(verificationUrl)
        );
    }

    public AccessToken verification(String token, HttpServletResponse response) {
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

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByToken(refreshToken);
        if (existingToken.isPresent()) {
            // Если токен уже существует, обновляем его (или можно удалить старый)
            RefreshToken tokenToUpdate = existingToken.get();
            tokenToUpdate.setExpiresAt(LocalDateTime.now().plusDays(7));
            refreshTokenRepository.save(tokenToUpdate);
        } else {
            RefreshToken dbRefreshToken = RefreshToken.builder()
                    .token(refreshToken)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .createdAt(LocalDateTime.now())
                    .build();
            refreshTokenRepository.save(dbRefreshToken);
        }

        response.setHeader(HttpHeaders.SET_COOKIE, "refreshToken=" + refreshToken + "; HttpOnly; Secure; SameSite=Strict; Max-Age=604800");

        return new AccessToken(accessToken);
    }

    public AccessToken signin(String email, String password, HttpServletResponse response) {
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

        response.setHeader(HttpHeaders.SET_COOKIE, "refreshToken=" + refreshToken + "; HttpOnly; Secure; SameSite=Strict; Max-Age=604800");

        return new AccessToken(accessToken);
    }

    public AccessToken refreshTokens(String inputUserRefreshToken, HttpServletResponse response) {
        final var user = userRepository.findByEmail(tokenUtil.getEmailFromToken(inputUserRefreshToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (tokenUtil.isTokenExpired(inputUserRefreshToken))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");

        String accessToken = tokenUtil.generateAccessToken(user.getEmail(), user.getRole().toString(), user.getId());
        String refreshToken = tokenUtil.generateRefreshToken(user.getEmail());
        RefreshToken dbRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(dbRefreshToken);

        response.setHeader(HttpHeaders.SET_COOKIE, "refreshToken=" + refreshToken + "; HttpOnly; Secure; SameSite=Strict; Max-Age=604800");

        return new AccessToken(accessToken);
    }

    // eyJhbGciOiJSUzI1NiIsImtpZCI6IjU2NGZlYWNlYzNlYmRmYWE3MzExYjlkOGU3M2M0MjgxOGYyOTEyNjQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIzNjM2ODkzNzcyMDEtaTdkMjNnMDhmMXFkZWs1c2hranVqczQ4Mmw4cGtqcm4uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiIzNjM2ODkzNzcyMDEtaTdkMjNnMDhmMXFkZWs1c2hranVqczQ4Mmw4cGtqcm4uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTAyMTI1OTkwMDgyODMzNzg5NDQiLCJlbWFpbCI6InJtLnRqLjc3N0BnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmJmIjoxNzM0MTQ2NjUxLCJuYW1lIjoicmFtaWwiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jSkZOUWJwaU1mZy1JRFZEeE1UcTItQmhSQjhPaFo5ZE5TTU9mOC1rNkZEbF9IeVBIVT1zOTYtYyIsImdpdmVuX25hbWUiOiJyYW1pbCIsImlhdCI6MTczNDE0Njk1MSwiZXhwIjoxNzM0MTUwNTUxLCJqdGkiOiJhN2RhMzQ5ZmE5ZDlmNzc4NWViOTQ5YTllNGNiODYyOGEyMmI0NzEyIn0.jYT8kNeEx3Vriq0afkCsoXCynstrZJv7kZQeMOYg223z3CV2PWdCOW7kYRA0Kr8Fc-MsdqOAUq6EtHQzUHRpy772b0ot1MEshAYVPh_o0GksMufznrOXLBngbjZ3wA_MqIK2F7F43GuCEm5QtFQCWeYRs0DMoJJ1O7LZx684DE6cOlh1wkGLBt_iYWNOVyFD6dCMXhroRHnNNGhdtNNO4Rkc6nBWFVg8rV3PZwgJC7cczws6D0MEt2a-E0t0-AqhZp3p3fJE9-wNFc1DEHxb7Errz0wFZ0DoaC9xqWyaGMJtQuzzA2Y1dU26YVqb0QxzZmZoqMWY5LMHu6JZNpMexQ
    public AccessToken googleLogin(String googleToken, HttpServletResponse response) throws Exception {
        String email = tokenUtil.getEmailFromGoogleToken(googleToken);

        System.out.println("+--------------------------------------------------------------------------------------+");
        System.out.println("|   EMAIL FROM GOOGLE TOKEN: " + email);
        System.out.println("+--------------------------------------------------------------------------------------+");

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();

            // Если пользователь существует, но не использует Google OAuth
            if (!user.getIsGoogleAuth() || user.getIsGoogleAuth() == null) {
                user.setIsGoogleAuth(true);
                user.setUpdatedAt(LocalDateTime.now());
                user = userRepository.save(user);
            }
        } else {
            // Если пользователь не найден, создаем нового
            user = User.builder()
                    .email(email)
                    .isGoogleAuth(true)
                    .isVerified(false)
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);
        }

        // Генерация токенов
        String accessToken = tokenUtil.generateAccessToken(user.getEmail(), user.getRole().toString(), user.getId());
        String refreshToken = tokenUtil.generateRefreshToken(user.getEmail());

        // Создание и сохранение рефреш-токена
        RefreshToken dbRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(dbRefreshToken);

        // Установка рефреш-токена в куки
        response.setHeader(HttpHeaders.SET_COOKIE, "refreshToken=" + refreshToken + "; HttpOnly; Secure; SameSite=Strict; Max-Age=604800");

        // Возврат Access Token
        return new AccessToken(accessToken);
    }

    public void passReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            String userEmail = userOpt.get().getEmail();
            String resetToken = urlUtil.generateRandomToken();

            PassResetToken resetTokenEntity = PassResetToken.builder()
                    .token(resetToken)
                    .isUsed(false)
                    .user(userOpt.get())
                    .createdAt(LocalDateTime.now())
                    .build();
            passResetTokenRepository.save(resetTokenEntity);
            String url = urlUtil.createPassResetUrlByToken(ApiPath.FRONTEND_BASE_URL_DEV.get(), resetToken);
            mailUtil.sendHtmlMessage(
                    userEmail,
                    MailTemplates.SUBJECT_RESET_PASSWORD.get(),
                    MailTemplates.BODY_RESET_PASSWORD.set(userEmail, url)
            );
            return;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email id");
    }

    public boolean passResetVerify(String token) {
        return passResetTokenRepository.existsByToken(token);
    }

    public void passResetConfirm(String password, String token) {
        // TODO: менять пароль | найти по токену юзера, ему поменять захэшированный пароль

        Optional<User> userOpt = passResetTokenRepository.findUserByToken(token);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token | No such user_id");
        }
        User user = userOpt.get();
        user.setPasswordHash(passwordUtil.hashPassword(password));
        userRepository.save(user);
        System.out.println("=================USER PASS SAVED SUCCESSFULLY " + password);

        PassResetToken resetToken = passResetTokenRepository.findByToken(token).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid or expired password reset token"));
        resetToken.setIsUsed(true);
        passResetTokenRepository.save(resetToken);
        System.out.println("=================resetToken.setIsUsed!!!");

    }


}