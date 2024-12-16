package dev.ramil21.lab4back.service;

import dev.ramil21.lab4back.dto.AccessToken;
import dev.ramil21.lab4back.model.RefreshToken;
import dev.ramil21.lab4back.model.Role;
import dev.ramil21.lab4back.model.User;
import dev.ramil21.lab4back.repository.RefreshTokenRepository;
import dev.ramil21.lab4back.repository.UserRepository;
import dev.ramil21.lab4back.util.PasswordUtil;
import dev.ramil21.lab4back.util.TokenUtil;
import dev.ramil21.lab4back.util.VerificationUtil;
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
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.getIsGoogleAuth()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User account already exists for provided email-id");
            }
        }

        String verificationToken = verificationUtil.generateVerificationToken();
        // TODO: вынести в ENUM http://localhost:8080 - как BASE_URL и все ENUMы всех путей тоже (вместе в один enum)
        String verificationUrl = verificationUtil.createVerificationUrlByToken("http://localhost:5174", verificationToken);
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

        //mailUtil.sendSimpleMessage(savedUser.getEmail(), "Subject", "Перейдите по ссылке чтобы подтвердить аккаунт: " + verificationUrl);
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
            tokenToUpdate.setExpiresAt(LocalDateTime.now().plusDays(7));  // Обновляем срок действия
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
            user = userOptional.get();  // Если пользователь найден, берем его из Optional
            if (!user.getIsGoogleAuth()) {
                user.setIsVerified(true);
                user = userRepository.save(user);
            }
        } else {
            user = User.builder() // Если пользователь не найден, создаем нового
                    .email(email)
                    .isVerified(true)
                    .isGoogleAuth(true)
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);
        }

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

}